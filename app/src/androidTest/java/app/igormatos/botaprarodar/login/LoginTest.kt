package app.igormatos.botaprarodar.login

import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import app.igormatos.botaprarodar.screens.login.LoginActivity
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
class LoginTest {

    @get:Rule
    val loginActivityRule = ActivityTestRule(LoginActivity::class.java)

    private lateinit var loginActivity: LoginActivity

    @Before
    fun setUp() {
        loginActivity = loginActivityRule.activity
    }

    @Test
    fun should1ReturnMessageWhenEmailIncorrect() {
        login {
            clickLogin()
            fillUserField("userIncorrect")
            clickNext()
        } verify {
            checkMessage("That email address isn't correct")
        }
    }

    @Test
    fun should2ReturnMessageWhenPasswordIncorrect() {
        login {
            clickLogin()
            fillUserField("brunotmg@gmail.com")
            clickNext()
            fillPasswordField("abc")
            clickSignIn()
        } verify {
            checkMessageIncorrectPassword()
        }
    }

    @Test
    fun should3RecoveryPassword() {
        login {
            clickLogin()
            fillUserField("brunotmg@gmail.com")
            clickNext()
            clickRecoveryPassword()
        } verify {
            checkMessage("brunotmg@gmail.com")
        }
    }

    @Test
    fun should4DoLoginSuccessful() {
        login {
            doLogin("brunotmg@gmail.com", "abcd1234")
            sleep(2000)
        }
    }

}