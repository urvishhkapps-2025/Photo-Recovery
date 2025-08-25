package com.Blue.photorecovery.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.Blue.photorecovery.adapter.common.RecoverHistoryAdapter
import com.Blue.photorecovery.common.PageVisibility
import com.Blue.photorecovery.common.SelectionHost
import com.Blue.photorecovery.common.UserDataManager
import com.Blue.photorecovery.databinding.FragmentPhotoBinding

class PhotoFragment : Fragment(), SelectionHost, PageVisibility {

    lateinit var binding: FragmentPhotoBinding
    private lateinit var adapter: RecoverHistoryAdapter
    private var updatingGlobal = false
    private var isSelectAll = false
    private var allRecovered: ArrayList<String> = ArrayList()
    private var count: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        count = arguments?.getInt(ARG_COUNT) ?: 0
        binding.apply {

            allRecovered = when (count) {
                1 -> UserDataManager.instance!!.getRecoverImages().reversed().toCollection(ArrayList())
                2 -> UserDataManager.instance!!.getRecoverVideo().reversed().toCollection(ArrayList())
                else -> arrayListOf()
            }

            updateEmptyState()

            adapter = RecoverHistoryAdapter(
                count,
                this@PhotoFragment.requireActivity(),
                allRecovered
            )

            val glm = GridLayoutManager(this@PhotoFragment.requireActivity(), 3)
            binding.recyclerView.layoutManager = glm
            binding.recyclerView.adapter = adapter

            setupSelectAllCheckbox()

        }

    }

    private fun updateEmptyState() {
        val empty = allRecovered.isEmpty()
        binding.layNotView.visibility = if (empty) View.VISIBLE else View.GONE
        binding.layView.visibility = if (empty) View.GONE else View.VISIBLE
        binding.textNoFount.text = when (count) {
            2 -> "No Videos Have Been Restored Yet"
            3 -> "No Audio Have Been Restored Yet"
            else -> "No Photos Have Been Restored Yet"
        }
    }

    private fun setupSelectAllCheckbox() {
        binding.cbPhoto.setOnCheckedChangeListener { _, isChecked ->
            if (updatingGlobal) return@setOnCheckedChangeListener
            adapter?.toggleSelectAll(isChecked)
            if (adapter?.areAllSelected() == true) {
                binding.textAllSelect.text = "Unselect All"
            } else {
                binding.textAllSelect.text = "Select All"
            }
        }

        // Listen for changes in adapter to update the select all checkbox
        adapter.onSelectionChangeListener =
            object : RecoverHistoryAdapter.OnSelectionChangeListener {
                override fun onSelectionChanged(selectedCount: Int, totalCount: Int) {
                    updateSelectAllText(selectedCount, totalCount)
                    (activity as? RecoverHistoryAdapter.OnSelectionChangeListener)
                        ?.onSelectionChanged(selectedCount, totalCount)
                }
            }
    }

    private fun updateSelectAllText(selectedCount: Int, totalCount: Int) {
        updatingGlobal = true
        isSelectAll = selectedCount == totalCount && totalCount > 0
        binding.textAllSelect.text = if (isSelectAll) "Unselect All" else "Select All"
        binding.cbPhoto.isChecked = isSelectAll
        updatingGlobal = false

    }

    override fun onDeleteRequest(): Int {
        val selected = adapter.getSelectedItems()   // List<String> of Uri strings
        if (selected.isEmpty()) return 0

        var deleted = 0
        val it = selected.iterator()
        while (it.hasNext()) {
            val uriString = it.next()
            allRecovered.remove(uriString)
            deleted++
        }

        if (count == 1) {
            UserDataManager.instance!!.setRecoverImages(allRecovered.reversed().toCollection(ArrayList()))
        } else if (count == 2) {
            UserDataManager.instance!!.setRecoverVideo(allRecovered.reversed().toCollection(ArrayList()))
        }

        adapter.planList = allRecovered
        adapter.selectAll(false)
        binding.textAllSelect.text = "Select All"
        binding.cbPhoto.isChecked = false
        binding.layNotView.visibility = if (allRecovered.isEmpty()) View.VISIBLE else View.GONE
        binding.layView.visibility = if (allRecovered.isEmpty()) View.GONE else View.VISIBLE

        return deleted
    }

    override fun onHidden() {
        adapter?.toggleSelectAll(false)
        if (adapter?.areAllSelected() == true) {
            binding.textAllSelect.text = "Unselect All"
        } else {
            binding.textAllSelect.text = "Select All"
        }
    }

    companion object {
        private const val ARG_COUNT = "arg_count"

        fun newInstance(count: Int): PhotoFragment {
            val f = PhotoFragment()
            f.arguments = Bundle().apply {
                putInt(ARG_COUNT, count)
            }
            return f
        }
    }

}