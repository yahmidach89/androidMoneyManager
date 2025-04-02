package com.ahmidach.epsi1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.Query
import java.util.Date
import kotlin.math.abs

class Graphique : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphique)

        // Initialisation du RecyclerView pour afficher la liste des transactions
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(mutableListOf())
        recyclerView.adapter = transactionAdapter

        // Initialisation du PieChart pour afficher un graphique circulaire des dépenses
        pieChart = findViewById(R.id.pieChart)

        // Initialisation du menu de navigation
        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Gestion des éléments du menu de navigation
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, History::class.java))
                    true
                }
                R.id.nav_graphics -> {
                    startActivity(Intent(this, Graphique::class.java))
                    true
                }
                R.id.nav_graphicsAdd -> {
                    startActivity(Intent(this, GraphiqueAjout::class.java))
                    true
                }
                /*R.id.nav_graphicsTracking -> {
                    startActivity(Intent(this, ExpenseTracking::class.java))
                    true
                }*/
                else -> true
            }
        }

        // Chargement des transactions et des données pour le graphique
        loadTransactionsFromFirebase()
        fetchTransactions()
    }

    // Récupération des transactions négatives (dépenses) depuis Firebase pour le graphique
    private fun loadTransactionsFromFirebase() {
        val transactions = mutableListOf<Transaction>()

        db.collection("transactions")
            .whereLessThan("amount", 0) // Filtrer uniquement les dépenses
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactions.add(transaction)
                }
                createPieChart(transactions) // Création du graphique une fois les données récupérées
            }
            .addOnFailureListener { e ->
                println("Erreur lors de la récupération des transactions: $e")
            }
    }

    // Création du graphique circulaire en fonction des dépenses enregistrées
    private fun createPieChart(transactions: List<Transaction>) {
        val entries = ArrayList<PieEntry>()
        val tagAmounts = mutableMapOf<String, Float>()

        // Regrouper les dépenses par tag et les additionner
        for (transaction in transactions) {
            val tag = transaction.tag.ifBlank { "Autres" } // Si le tag est vide, l'affecter à "Autres"
            tagAmounts[tag] = abs(((tagAmounts[tag] ?: 0f) + transaction.amount).toFloat())
        }

        // Ajouter les données au graphique
        for ((tag, amount) in tagAmounts) {
            entries.add(PieEntry(amount, tag))
        }

        // Personnalisation du PieChart
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextSize = 16f
        pieChart.setEntryLabelTextSize(14f)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.legend.textSize = 14f
        pieChart.legend.xEntrySpace = 25f
        pieChart.invalidate() // Rafraîchir le graphique
    }

    // Récupération des transactions pour affichage dans le RecyclerView
    @SuppressLint("NewApi")
    private fun fetchTransactions() {
        db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("amount") // Trie les transactions par montant après la date
            .whereLessThan("amount", 0) // Ne récupérer que les transactions négatives
            .limit(10) // Limiter à 10 résultats
            .get()
            .addOnSuccessListener { result ->
                val transactions = mutableListOf<Transaction>()
                for (document in result) {
                    val amount = document.getDouble("amount") ?: 0.0
                    val reason = document.getString("reason") ?: "Aucune raison"
                    val tag = document.getString("tag") ?: "Aucun tag"
                    val date = document.getDate("date") ?: Date()

                    transactions.add(Transaction(document.id, amount, reason, tag, date))
                }
                transactionAdapter.updateData(transactions)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Erreur lors du chargement des transactions", e)
                Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Gérer l'ouverture du menu latéral
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
