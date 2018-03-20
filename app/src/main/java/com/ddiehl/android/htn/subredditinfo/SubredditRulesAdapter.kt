package com.ddiehl.android.htn.subredditinfo

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.subredditinfo.SubredditRulesAdapter.VH.Companion.LAYOUT_RES_ID
import com.ddiehl.android.htn.view.markdown.HtmlParser
import rxreddit.model.SubredditRule

/**
 * TODO documentation
 */
class SubredditRulesAdapter(
        private val htmlParser: HtmlParser
) : RecyclerView.Adapter<SubredditRulesAdapter.VH>() {

    private val rules: MutableList<SubredditRule> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(LAYOUT_RES_ID, parent, false)
        return VH(view, htmlParser)
    }

    override fun getItemCount() = rules.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val subredditRule = rules[position]
        holder.bind(subredditRule)
    }

    fun setRules(rules: List<SubredditRule>) {
        this.rules.addAll(rules)
    }

    class VH(
            itemView: View,
            private val htmlParser: HtmlParser
    ) : RecyclerView.ViewHolder(itemView) {

        companion object {
            @LayoutRes val LAYOUT_RES_ID = R.layout.subreddit_rule
        }

        private val shortNameView = itemView.findViewById<TextView>(R.id.short_name)
        private val categoryView = itemView.findViewById<TextView>(R.id.category)
        private val descriptionView = itemView.findViewById<TextView>(R.id.description)

        fun bind(rule: SubredditRule) {
            val shortName = rule.shortName
            shortNameView.text = itemView.context.getString(
                    R.string.subreddit_rule_short_name,
                    (adapterPosition + 1).toString(),
                    shortName
            )

            val category = rule.kind
            categoryView.setText(getTextForCategory(category))

            val descriptionHtml = rule.descriptionHtml
            if (descriptionHtml != null) {
                val description = descriptionHtml.trim { it <= ' ' }
                val parsedDescription = htmlParser.convert(description)
                descriptionView.setText(parsedDescription)
            }
        }

        private fun getTextForCategory(category: String): String {
            return when (category) {
                "link" -> itemView.context.getString(R.string.subreddit_category_link)
                "comment" -> itemView.context.getString(R.string.subreddit_category_comment)
                "all" -> itemView.context.getString(R.string.subreddit_category_all)
                else -> itemView.context.getString(R.string.subreddit_category_all)
            }
        }
    }
}
