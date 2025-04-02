package com.ahmidach.epsi1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class History : AppCompatActivity() {

    // Déclaration des variables pour l'UI et la base de données Firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialisation du RecyclerView et de son adaptateur
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        transactionAdapter = TransactionAdapter(mutableListOf())
        recyclerView.adapter = transactionAdapter

        // Configuration du menu latéral (DrawerLayout)
        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Gestion de la navigation dans le menu latéral
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

                R.id.nav_graphicsTracking -> {
                    val intent = Intent(this, ExpenseTracking::class.java)
                    startActivity(intent)
                    return@setNavigationItemSelectedListener true
                }

                else -> return@setNavigationItemSelectedListener true
            }
        }

        // Chargement des transactions
        fetchTransactions()
    }

    @SuppressLint("NewApi")
    private fun fetchTransactions() {
        // Récupération des transactions depuis Firestore, triées par date décroissante et limitées à 30
        db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { result ->
                val transactions = mutableListOf<Transaction>()
                for (document in result) {
                    // Récupération des données avec des valeurs par défaut en cas de null
                    val amount = document.getDouble("amount") ?: 0.0
                    val reason = document.getString("reason") ?: "Aucune raison"
                    val tag = document.getString("tag") ?: "Aucun tag"
                    val date = document.getDate("date") ?: Date()

                    // Ajout de la transaction à la liste
                    transactions.add(Transaction(document.id, amount, reason, tag, date))
                }

                // Mise à jour de l'adaptateur avec les nouvelles transactions
                transactionAdapter.updateData(transactions)
            }
            .addOnFailureListener {
                // Affichage d'un message d'erreur en cas d'échec
                Toast.makeText(this, "Erreur lors du chargement des transactions", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Gestion de l'ouverture et fermeture du menu latéral
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
