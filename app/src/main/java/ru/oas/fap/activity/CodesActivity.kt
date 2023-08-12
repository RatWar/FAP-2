package ru.oas.fap.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ru.oas.fap.AllViewModel
import ru.oas.fap.CodesListAdapter
import ru.oas.fap.databinding.ActivityCodesBinding
import ru.oas.fap.room.CodesData

class CodesActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private lateinit var binding: ActivityCodesBinding
    private var mBarcode: String = ""
    private var mNumDoc: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCodesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val codesRecyclerView = binding.recyclerCodesList
        val codesAdapter = CodesListAdapter(this)
        codesRecyclerView.adapter = codesAdapter
        codesRecyclerView.layoutManager = LinearLayoutManager(this)

        val onCodesClickListener = object : CodesListAdapter.OnCodesClickListener {
            override fun onCodesClick(codes: CodesData) {
                mAllViewModel.deleteSGTIN(mNumDoc, codes.sgtin)
            }
        }
        codesAdapter.codesAdapter(onCodesClickListener)

        val intent = intent
        mBarcode = intent.getStringExtra("Barcode").toString()
        mNumDoc = intent.getIntExtra("NumDoc", 0)

        mAllViewModel = ViewModelProvider(this).get(AllViewModel::class.java)
        mAllViewModel.setNumDocAndBarcode(mNumDoc, mBarcode)
        mAllViewModel.mAllCodes.observe(this) { codes ->
            codes?.let { codesAdapter.setCodes(it) }
        }
    }
}