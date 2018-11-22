package com.ddiehl.android.htn.listings.report

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.view.getDelegate
import timber.log.Timber

class ReportDialog : DialogFragment() {

    companion object {

        const val TAG = "ReportDialog"
        private const val ARG_RULES = "rules"
        private const val ARG_SITE_RULES = "siteRules"

        @JvmStatic
        fun newInstance(rules: Array<String>, siteRules: Array<String>): ReportDialog {
            return ReportDialog().apply {
                arguments = Bundle().apply {
                    putStringArray(ARG_RULES, rules)
                    putStringArray(ARG_SITE_RULES, siteRules)
                }
            }
        }
    }

    private val rules: Array<String> by lazy { arguments!!.getStringArray(ARG_RULES) }
    private val siteRules: Array<String> by lazy { arguments!!.getStringArray(ARG_SITE_RULES) }

    internal var selectedIndex = -1
    internal var selectedButton: RadioButton? = null
    internal var listener: Listener? = null

    interface Listener {

        fun onRuleSubmitted(rule: String)

        fun onSiteRuleSubmitted(rule: String)

        fun onOtherSubmitted(reason: String)

        fun onCancelled()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = getDelegate()
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get report options
        val reportOptions = getReportOptions(rules, siteRules)
        val numOptions = reportOptions.size

        // Create dialog with options
        val inflater = LayoutInflater.from(context)

        // Inflate parent RadioGroup
        @SuppressLint("InflateParams") val view =
            inflater.inflate(R.layout.report_dialog_view, null, false) as ViewGroup
        val parent = view.findViewById<RadioGroup>(R.id.dialog_view_group)

        // Inflate 'other' dialog item
        val otherOptionView = inflater.inflate(R.layout.report_dialog_view_edit_item, parent, false)
        val otherSelector = otherOptionView.findViewById<RadioButton>(R.id.report_choice_item_selector)

        // Add rest of option views
        for (i in 0 until numOptions) {
            // Inflate item view
            val optionView = inflater.inflate(R.layout.report_dialog_view_choice_item, parent, false)

            // Get RadioButton and set ID so the RadioGroup can properly manage checked state
            val selector = optionView.findViewById<RadioButton>(R.id.report_choice_item_selector)
            selector.id = i

            // Set checked state change listener that caches selected index
            selector.setOnCheckedChangeListener { _, isChecked ->
//                otherSelector.isChecked = false
                if (isChecked) {
                    selectedIndex = i
                    selectedButton = selector
                }
                clearAllChecks(parent)
            }

            // Set text for option
            val selectorText = optionView.findViewById<TextView>(R.id.report_choice_text)
            selectorText.text = reportOptions[i]

            // Add view to parent
            parent.addView(optionView)
        }

        // Set checked listener for 'other' field
        otherSelector.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedIndex = numOptions
                selectedButton = otherSelector
//                otherSelector.isChecked = true
            }
            clearAllChecks(parent)
        }

        // Add other option view
        parent.addView(otherOptionView)

        // Build AlertDialog from custom view
        return AlertDialog.Builder(context!!)
            .setTitle(R.string.report_menu_title)
            .setPositiveButton(R.string.report_submit, onSubmit())
            .setNegativeButton(R.string.report_cancel, onCancelButton())
            .setView(view)
            .create()
    }

    private fun clearAllChecks(view: View) {
        Timber.d("[dcd] clearing all checks for $view")
        if (view is RadioButton && view != selectedButton) {
            Timber.d("[dcd] view is RadioButton")
            view.isChecked = false
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                clearAllChecks(child)
            }
        }
    }

    private fun getReportOptions(rules: Array<String>, siteRules: Array<String>): Array<String?> {
        // Concatenate all options into single array
        val reportOptions = arrayOfNulls<String>(rules.size + siteRules.size)
        System.arraycopy(rules, 0, reportOptions, 0, rules.size)
        System.arraycopy(siteRules, 0, reportOptions, rules.size, siteRules.size)
        return reportOptions
    }

    private fun onSubmit(): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { _, _ ->
            // If index is in rules array, submit the rule
            if (selectedIndex < rules.size) {
                submit(rules[selectedIndex], null, null)
            } else if (selectedIndex < rules.size + siteRules.size) {
                submit(null, siteRules[selectedIndex - rules.size], null)
            }
            // Otherwise, submit the other reason
            // If index is in site rules array, submit the site rule
            else {
                val otherText = dialog.findViewById<EditText>(R.id.report_choice_edit_text)
                val input = otherText.text.toString().trim { it <= ' ' }
                submit(null, null, input)
            }
        }
    }

    internal fun onCancelButton(): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { dialogInterface, _ -> onCancel(dialogInterface) }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        listener!!.onCancelled()
    }

    private fun submit(rule: String?, siteRule: String?, other: String?) {
        if (other != null) {
            listener!!.onOtherSubmitted(other)
            dismiss()
        } else if (rule != null) {
            listener!!.onRuleSubmitted(rule)
            dismiss()
        } else if (siteRule != null) {
            listener!!.onSiteRuleSubmitted(siteRule)
            dismiss()
        }
    }
}
