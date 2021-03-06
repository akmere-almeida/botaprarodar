package app.igormatos.botaprarodar.presentation.login

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import app.igormatos.botaprarodar.R
import app.igormatos.botaprarodar.domain.model.RequestException
import app.igormatos.botaprarodar.domain.model.UserCommunityInfo
import app.igormatos.botaprarodar.domain.model.community.Community
import app.igormatos.botaprarodar.data.network.RequestError
import com.brunotmgomes.ui.SnackbarModule
import com.brunotmgomes.ui.ViewEvent
import com.google.android.material.snackbar.Snackbar
import io.mockk.*
import org.hamcrest.CoreMatchers.`is`
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module


class LoginActivityRobot(
    private val emailNotVerified: Boolean = false
) {

    private val scenario: ActivityScenario<LoginActivity>
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val testModule: Module
    private val mockEmailSnackbar = mockk<Snackbar>(relaxed = true) {
        every {
            setAction(
                any<Int>(),
                any()
            )
        } returns this
    }
    private val mockLoginErrorSnackbar = mockk<Snackbar>(relaxed = true)
    private val loadCommunitiesSnackbarClickAction: CapturingSlot<View.OnClickListener> = slot()
    private val mockLoadCommunitiesErrorSnackbar = mockk<Snackbar>(relaxed = true) {
        every {
            setAction(
                any<Int>(),
                capture(loadCommunitiesSnackbarClickAction)
            )
        } returns this
    }

    private val navigator: LoginActivityNavigator = mockk(relaxed = true)

    private val viewModel: LoginActivityViewModelFake

    init {
        viewModel = LoginActivityViewModelFake(
            showResendEmailSnackBar = if (emailNotVerified) {
                MutableLiveData(ViewEvent(true))
            } else {
                MutableLiveData()
            },
        )

        testModule = module(override = true) {
            single<SnackbarModule> {
                val emailError = context.getString(R.string.login_confirm_email_error)
                val loginError = context.getString(R.string.login_error)
                val communityErrorText = context.getString(R.string.login_load_communities_error)
                mockk {
                    every { make(any(), emailError, any()) } returns mockEmailSnackbar
                    every { make(any(), loginError, any()) } returns mockLoginErrorSnackbar
                    every {
                        make(
                            any(),
                            communityErrorText,
                            any()
                        )
                    } returns mockLoadCommunitiesErrorSnackbar
                }
            }
            single<LoginActivityNavigator> {
                navigator
            }
            viewModel<LoginActivityViewModel> {
                viewModel
            }
        }
        loadKoinModules(
            testModule
        )

        Intents.init()
        scenario = launchActivity()
    }

    private fun addCommunityButton(): ViewInteraction {
        val text = context.getString(R.string.add_community)
        return onView(withText(text)).inRoot(isDialog())
    }

    fun triggerNavigateMain() {
        viewModel.navigateMain.postValue(ViewEvent(true))
    }

    fun setLoading(loading: Boolean) {
        viewModel.loading.postValue(loading)
    }

    fun clickLogin(isSuccess: Boolean = true) {
        val resultData = Intent()
        val result: Instrumentation.ActivityResult = if (isSuccess) {
            Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        } else {
            Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData)
        }
        Intents.intending(toPackage("app.igormatos.botaprarodar")).respondWith(result)

        onView(withId(R.id.login_button)).perform(click())
    }

    fun clickCommunity(community: Community) {
        onView(withText(community.name)).perform(click())
    }

    fun clickAddCommunity() {
        addCommunityButton().perform(click())
    }

    fun clickRetryLoadCommunities() {
        loadCommunitiesSnackbarClickAction.captured.onClick(mockk())
    }

    fun triggerCommunitiesLoaded(communities: UserCommunityInfo) {
        viewModel.loadedCommunities.postValue(ViewEvent(Result.success(communities)))
    }

    fun triggerCommunitiesLoadError(err: RequestError = RequestError.DEFAULT) {
        viewModel.loadedCommunities.postValue(ViewEvent(Result.failure(RequestException(err))))
    }

    fun startResendEmail() {
        viewModel.sendEmailVerification()
    }

    fun finishResendEmail() {
        viewModel.finishEmailResend()
    }

    fun verifyNavigatedToMainActivity() {
        viewAssertion {
            verify { navigator.goToMainActivity(any()) }
        }
    }

    fun verifyNavigatedToAddCommunity() {
        viewAssertion {
            verify { navigator.goToAddCommunityActivity(any()) }
        }
    }

    fun verifyLoggedIn(loggedIn: Boolean) {
        viewAssertion {
            assertThat(viewModel.loggedIn, `is`(loggedIn))
        }
    }

    fun verifyLoginErrorSnackbarShown() {
        viewAssertion {
            verify { mockLoginErrorSnackbar.show() }
        }
    }

    fun verifyLoadCommunitiesErrorSnackbarShown() {
        viewAssertion {
            verify { mockLoadCommunitiesErrorSnackbar.show() }
        }
    }

    fun verifyEmailSnackbarShown(shown: Boolean) {
        viewAssertion {
            if (shown) {
                verify { mockEmailSnackbar.show() }
            } else {
                verify(exactly = 0) { mockEmailSnackbar.show() }
            }
        }
    }

    fun verifyEmailSnackbarDismissed() {
        viewAssertion {
            verify { mockEmailSnackbar.dismiss() }
        }
    }

    fun verifyLoadingDialogShown(isShown: Boolean) {
        onView(withId(R.id.loading_dialog)).check(isShownMatcher(isShown))
    }

    fun verifyNoCommunityDialogShown() {
        val text = context.getString(R.string.login_no_communities_allowed)
        onView(withText(text)).check(isShownMatcher(true))
    }

    fun verifyCommunityShown(community: Community) {
        onView(withText(community.name)).check(matches(isDisplayed()))
    }

    fun verifyCommunityChosen(community: Community) {
        viewAssertion {
            assertThat(viewModel.chosenCommunity, `is`(community))
        }
    }

    fun verifyAddCommunityButtonShown(isShown: Boolean) {
        addCommunityButton().check(isShownMatcher(isShown))
    }

    fun verifyReloadCommunitiesTriggered() {
        viewAssertion {
            assertThat(viewModel.retryLoadedCommunity, `is`(true))
        }
    }

    fun verifyLoadCommunitiesErrorSnackbarDismissed() {
        verify { mockLoadCommunitiesErrorSnackbar.dismiss() }
    }

    private fun isShownMatcher(isShown: Boolean): ViewAssertion {
        return if (isShown) {
            matches(isDisplayed())
        } else {
            doesNotExist()
        }

    }

    // View assertions must use the Main Thread
    private fun viewAssertion(block: () -> Unit) {
        scenario.onActivity {
            block()
        }
    }

    fun finish() {
        scenario.close()
        Intents.release()
        unloadKoinModules(testModule)
    }
}