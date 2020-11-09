package app.igormatos.botaprarodar.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.igormatos.botaprarodar.R
import app.igormatos.botaprarodar.data.model.Bicycle
import app.igormatos.botaprarodar.local.Preferences
import app.igormatos.botaprarodar.network.FirebaseHelper
import app.igormatos.botaprarodar.network.RequestListener
import app.igormatos.botaprarodar.screens.BicycleAdapterListener
import app.igormatos.botaprarodar.screens.BicyclesAdapter
import app.igormatos.botaprarodar.screens.addbicycle.AddBikeActivity
import kotlinx.android.synthetic.main.fragment_list.*

class BicyclesFragment : Fragment(), BicycleAdapterListener {

    lateinit var bicycleAdapter: BicyclesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_list, container, false)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bicycleAdapter = BicyclesAdapter(bicycleAdapterListener = this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addItemFab.setOnClickListener {
            val intent = Intent(it.context, AddBikeActivity::class.java)
            startActivity(intent)
        }

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.adapter = bicycleAdapter

        val joinedCommunityId = Preferences.getJoinedCommunity(context!!).id!!
        FirebaseHelper.getBicycles(joinedCommunityId, listener = object : RequestListener<Bicycle> {
            override fun onChildChanged(result: Bicycle) {
                bicycleAdapter.updateItem(result)
            }

            override fun onChildAdded(result: Bicycle) {
                bicycleAdapter.addItem(result)
            }

            override fun onChildRemoved(result: Bicycle) {
                bicycleAdapter.removeItem(result)
            }

        })
    }

    override fun onBicycleClicked(bicycle: Bicycle) {
        Toast.makeText(context!!, bicycle.name, Toast.LENGTH_SHORT).show()
    }

    override fun onBicycleLongClicked(bicycle: Bicycle) : Boolean {
        Toast.makeText(context!!, bicycle.name, Toast.LENGTH_SHORT).show()
        return true
    }
}