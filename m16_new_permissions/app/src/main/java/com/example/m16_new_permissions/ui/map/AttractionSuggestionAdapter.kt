package com.example.m16_new_permissions.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.example.m16_new_permissions.R
import com.example.m16_new_permissions.domain.model.Attraction

/**
 * Подсказки для поля поиска метки: по набранным символам показывает подходящие метки.
 *
 * Сам подбор вариантов делает [search] — здесь только отрисовка списка. Так правило поиска
 * остаётся во ViewModel и не зависит от того, каким виджетом его показывают.
 */
class AttractionSuggestionAdapter(
    context: Context,
    private val search: (String) -> List<Attraction>
) : ArrayAdapter<Attraction>(context, R.layout.item_attraction_suggestion), Filterable {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: inflater.inflate(R.layout.item_attraction_suggestion, parent, false)
        val attraction = getItem(position)

        view.findViewById<TextView>(R.id.suggestionNameTextView).text = attraction?.name.orEmpty()

        // Вторая строка помогает различить метки с похожими названиями
        val details = view.findViewById<TextView>(R.id.suggestionDetailsTextView)
        val description = attraction?.description?.trim().orEmpty()
        if (description.isEmpty()) {
            details.visibility = View.GONE
        } else {
            details.visibility = View.VISIBLE
            details.text = description
        }

        return view
    }

    override fun getFilter(): Filter = object : Filter() {

        // Выполняется в отдельном потоке, поэтому здесь только чтение уже загруженного списка
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val matches = search(constraint?.toString().orEmpty())
            return FilterResults().apply {
                values = matches
                count = matches.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            @Suppress("UNCHECKED_CAST")
            val matches = results?.values as? List<Attraction> ?: emptyList()

            setNotifyOnChange(false)
            clear()
            addAll(matches)
            notifyDataSetChanged()
        }

        // Иначе в поле поиска после выбора подставится toString() модели
        override fun convertResultToString(resultValue: Any?): CharSequence =
            (resultValue as? Attraction)?.name.orEmpty()
    }
}
