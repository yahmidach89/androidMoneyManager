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

class GraphiqueAjout : AppCompatActivity() {
    // Déclaration des variables pour les composants UI et Firebase
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graphique_ajout)

        // Initialisation des composants UI
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(mutableListOf())
        recyclerView.adapter = transactionAdapter
        pieChart = findViewById(R.id.pieChart)

        // Configuration du menu latéral (DrawerLayout)
        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Gestion de la navigation du menu latéral
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, History::class.java))
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_graphics -> {
                    startActivity(Intent(this, Graphique::class.java))
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_graphicsAdd -> {
                    startActivity(Intent(this, GraphiqueAjout::class.java))
                    return@setNavigationItemSelectedListener true
                }
                /*R.id.nav_graphicsTracking -> {
                    startActivity(Intent(this, ExpenseTracking::class.java))
                    return@setNavigationItemSelectedListener true
                }*/
                else -> return@setNavigationItemSelectedListener true
            }
        }

        // Chargement des transactions et mise à jour de l'interface
        loadTransactionsFromFirebase()
        fetchTransactions()
    }

    // Fonction pour charger les transactions depuis Firebase et générer le graphique
    private fun loadTransactionsFromFirebase() {
        val transactions = mutableListOf<Transaction>()

        db.collection("transactions").whereGreaterThan("amount", 0).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactions.add(transaction)
                }
                // Création du graphique après récupération des données
                createPieChart(transactions)
            }
            .addOnFailureListener { e ->
                println("Error getting transactions: $e") // Affichage de l'erreur en console
            }
    }

    // Fonction pour créer et mettre à jour le graphique circulaire
    private fun createPieChart(transactions: List<Transaction>) {
        val entries = ArrayList<PieEntry>()
        val tagAmounts = mutableMapOf<String, Float>()

        // Calcul des montants totaux par catégorie (tag)
        for (transaction in transactions) {
            val tag = transaction.tag.ifBlank { "Autres" } // Si le tag est vide, on met "Autres"
            tagAmounts[tag] = abs(((tagAmounts[tag] ?: 0f) + transaction.amount).toFloat())
        }

        // Ajout des valeurs au graphique
        for ((tag, amount) in tagAmounts) {
            entries.add(PieEntry(amount, tag))
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList() // Appliquer des couleurs prédéfinies
        dataSet.valueTextSize = 16f
        pieChart.setEntryLabelTextSize(14f)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false // Désactiver la description du graphique
        pieChart.legend.textSize = 14f
        pieChart.setEntryLabelTextSize(14f)
        pieChart.legend.xEntrySpace = 25f
        pieChart.invalidate() // Rafraîchir l'affichage du graphique
    }

    @SuppressLint("NewApi")
    private fun fetchTransactions() {
        db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("amount") // On trie d'abord par date puis par montant
            .whereGreaterThan("amount", 0) // On filtre les montants supérieurs à 0
            .limit(10) // On limite à 10 transactions
            .get()
            .addOnSuccessListener { result ->
                val transactions = mutableListOf<Transaction>()
                for (document in result) {
                    // Récupération des valeurs avec des valeurs par défaut si null
                    val amount = document.getDouble("amount") ?: 0.0
                    val reason = document.getString("reason") ?: "Aucune raison"
                    val tag = document.getString("tag") ?: "Aucun tag"
                    val date = document.getDate("date") ?: Date()

                    transactions.add(Transaction(document.id, amount, reason, tag, date))
                }
                // Mise à jour de l'adaptateur avec les nouvelles données
                transactionAdapter.updateData(transactions)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Erreur lors du chargement des transactions", e)
                Toast.makeText(this, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Gestion de la sélection des éléments du menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
