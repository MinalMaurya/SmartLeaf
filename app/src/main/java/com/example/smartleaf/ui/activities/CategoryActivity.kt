package com.example.smartleaf.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartleaf.R
import com.example.smartleaf.ui.adapters.CategoryAdapter
import com.example.smartleaf.ui.models.Category
import com.example.smartleaf.ui.utils.LocaleHelper

class CategoryActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val search = findViewById<EditText>(R.id.etSearch)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_categories)
        val empty = findViewById<LinearLayout>(R.id.llEmptyState)

        val categories = listOf(
            Category("c1", "Cereals"),
            Category("c2", "Pulses"),
            Category("c3", "Vegetables"),
            Category("c4", "Fruits"),
            Category("c5", "Flowers"),
            Category("c6", "Oil Crops"),
            Category("c7", "Medicinal Plants"),
            Category("c8", "Plantation Crops")
        )

        val adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this, PlantListActivity::class.java)
            intent.putExtra("categoryId", category.id)
            intent.putExtra("categoryName", category.name)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        search.addTextChangedListener { editable ->
            val query = editable?.toString().orEmpty()

            val filtered = categories.filter { c ->
                c.name.contains(query, ignoreCase = true)
            }

            adapter.updateList(filtered)
            empty.visibility = if (filtered.isEmpty()) LinearLayout.VISIBLE else LinearLayout.GONE
        }
    }
}