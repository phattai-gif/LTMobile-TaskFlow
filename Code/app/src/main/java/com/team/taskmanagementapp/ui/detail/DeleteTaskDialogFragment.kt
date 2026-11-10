package com.team.taskmanagementapp.ui.detail

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.team.taskmanagementapp.R

class DeleteTaskDialogFragment : DialogFragment() {

    var onDeleteConfirmed: ((Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val isRecurring = arguments?.getBoolean(ARG_IS_RECURRING) ?: false

        return if (isRecurring) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.task_detail_delete_recurring_title)
                .setMessage(R.string.task_detail_delete_recurring_msg)
                .setNegativeButton(R.string.task_detail_delete_only_this) { _, _ ->
                    onDeleteConfirmed?.invoke(DELETE_ONLY_THIS)
                }
                .setPositiveButton(R.string.task_detail_delete_all_occurrences) { _, _ ->
                    onDeleteConfirmed?.invoke(DELETE_ALL)
                }
                .setNeutralButton(R.string.task_detail_delete_confirm_negative, null)
                .create()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.task_detail_delete_confirm_title)
                .setMessage(R.string.task_detail_delete_confirm_msg)
                .setNegativeButton(R.string.task_detail_delete_confirm_negative, null)
                .setPositiveButton(R.string.task_detail_delete_confirm_positive) { _, _ ->
                    onDeleteConfirmed?.invoke(DELETE_NORMAL)
                }
                .create()
        }
    }

    companion object {
        const val TAG = "DeleteTaskDialogFragment"
        const val DELETE_NORMAL = 0
        const val DELETE_ONLY_THIS = 1
        const val DELETE_ALL = 2

        private const val ARG_IS_RECURRING = "arg_is_recurring"

        fun newInstance(isRecurring: Boolean): DeleteTaskDialogFragment {
            return DeleteTaskDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_RECURRING, isRecurring)
                }
            }
        }
    }
}
