package com.ahmidach.epsi1

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
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.Query
import java.util.Date
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseTracking : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_tracking)

        // Initialisation du RecyclerView pour afficher les transactions
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(mutableListOf())
        recyclerView.adapter = transactionAdapter

        // Initialisation du graphique en ligne
        lineChart = findViewById(R.id.lineChart)

        // Configuration du menu de navigation
        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Gestion des clics sur les éléments du menu de navigation
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_history -> {
                    val intent = Intent(this, History::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_graphics -> {
                    val intent = Intent(this, Graphique::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }
                R.id.nav_graphicsAdd -> {
                    val intent = Intent(this, GraphiqueAjout::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }
                /*R.id.nav_graphicsTracking -> {
                    val intent = Intent(this, ExpenseTracking::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }*/
                else -> return@setNavigationItemSelectedListener true
            }
        }

        // Chargement des transactions depuis Firebase et affichage des données
        loadTransactionsFromFirebase()
        fetchTransactions()
    }

    // Récupère les transactions depuis Firebase pour les afficher dans le graphique
    private fun loadTransactionsFromFirebase() {
        val transactions = mutableListOf<Transaction>()
        db.collection("transactions")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactions.add(transaction)
                }
                createLineChart(transactions) // Création du graphique après récupération des données
            }
            .addOnFailureListener { e ->
                println("Error getting transactions: $e")
            }
    }

    // Crée et personnalise un graphique en ligne pour afficher l'évolution des transactions
    private fun createLineChart(transactions: List<Transaction>) {
        val entries = mutableListOf<Entry>()
        val currentDate = Date().time
        val startDate = currentDate - (30L * 24 * 60 * 60 * 1000) // Transactions des 30 derniers jours

        var totalAmount = 0f
        var index = 0f
        val xLabels = mutableListOf<String>()

        // Ajout des données des transactions au graphique
        for (transaction in transactions) {
            val transactionDate = transaction.date?.time ?: 0L
            if (transactionDate in startDate..currentDate) {
                totalAmount += transaction.amount.toFloat()
                entries.add(Entry(index, totalAmount))
                xLabels.add(formatDate(transaction.date!!))
                index += 1f
            }
        }

        // Personnalisation des données du graphique
        val dataSet = LineDataSet(entries, "Solde des transactions")
        dataSet.color = ColorTemplate.COLORFUL_COLORS[0]
        dataSet.setCircleColor(ColorTemplate.COLORFUL_COLORS[1])
        dataSet.circleRadius = 6f
        dataSet.setDrawFilled(true)
        dataSet.fillColor = ColorTemplate.getHoloBlue()
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = ColorTemplate.getHoloBlue()
        dataSet.valueTextSize = 12f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.animateX(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

        // Configuration de l'axe X
        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // Configuration de l'axe Y
        val yAxis: YAxis = lineChart.axisLeft
        yAxis.setDrawLabels(true)
        yAxis.setDrawAxisLine(true)
        yAxis.setDrawGridLines(true)

        lineChart.axisRight.isEnabled = false

        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = true
        lineChart.legend.textColor = getColor(R.color.black)
        lineChart.invalidate() // Rafraîchir le graphique
    }

    // Formate une date pour l'affichage sur le graphique
    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("dd/MM", Locale.getDefault())
        return format.format(date)
    }

    // Récupère les transactions depuis Firebase pour les afficher dans la liste
    private fun fetchTransactions() {
        db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(10)
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

    // Gère les interactions avec la barre de navigation
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
