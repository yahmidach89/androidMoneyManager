package com.ahmidach.epsi1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class MainActivity : AppCompatActivity() {
    // Déclaration des variables pour les éléments de l'interface utilisateur et Firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var tvCounterMoney: TextView
    private lateinit var etNumberEdit: EditText
    private lateinit var etReasonEdit: EditText
    private lateinit var etTagEdit: EditText
    private lateinit var btnSubmitAdd: Chip
    private lateinit var btnSubmitSub: Chip
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des composants de l'interface utilisateur
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        transactionAdapter = TransactionAdapter(mutableListOf())
        recyclerView.adapter = transactionAdapter
        tvCounterMoney = findViewById(R.id.tv_counterMoney)
        etNumberEdit = findViewById(R.id.et_numberEdit)
        etReasonEdit = findViewById(R.id.et_reasonEdit)
        etTagEdit = findViewById(R.id.et_tagEdit)
        btnSubmitAdd = findViewById(R.id.btn_submitAdd)
        btnSubmitSub = findViewById(R.id.btn_submitSub)

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

        // Chargement des transactions et mise à jour du solde
        fetchTransactions()
        updateBalance()
        setupButtons()
    }

    private fun setupButtons() {
        // Gestion du bouton d'ajout d'une transaction
        btnSubmitAdd.setOnClickListener {
            val amount = getAmount()
            val reason = etReasonEdit.text.toString()
            val tag = etTagEdit.text.toString()
            addTransaction(amount, reason, tag)
        }

        // Gestion du bouton de soustraction d'une transaction
        btnSubmitSub.setOnClickListener {
            val amount = -getAmount()
            val reason = etReasonEdit.text.toString()
            val tag = etTagEdit.text.toString()
            addTransaction(amount, reason, tag)
        }
    }

    private fun getAmount(): Double {
        // Récupération du montant saisi par l'utilisateur, avec une valeur par défaut de 0.0
        return etNumberEdit.text.toString().toDoubleOrNull() ?: 0.0
    }

    private fun addTransaction(amount: Double, reason: String, tag: String) {
        // Création d'une nouvelle transaction avec ses attributs
        val transaction = hashMapOf(
            "amount" to amount,
            "reason" to reason,
            "date" to Date(),
            "tag" to tag
        )

        // Ajout de la transaction à la base de données Firestore
        db.collection("transactions").add(transaction).addOnSuccessListener {
            updateBalance()
            fetchTransactions()
        }
            .addOnCompleteListener {
                it.result
            }
            .addOnFailureListener {
                it.cause
            }
    }

    @SuppressLint("DefaultLocale")
    private fun updateBalance() {
        // Récupération et calcul du solde total à partir des transactions
        db.collection("transactions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var total = 0.0
                for (document in task.result!!) {
                    total += document.getDouble("amount") ?: 0.0
                }

                // Mise à jour du texte affichant le solde
                tvCounterMoney.text = String.format("%.2f€", total)

                // Changement du fond du solde en fonction du montant
                val backgroundRes = when {
                    total > 0 -> R.drawable.circle_background_green
                    total < 0 -> R.drawable.circle_background_red
                    else -> R.drawable.circle_background_grey
                }
                tvCounterMoney.setBackgroundResource(backgroundRes)

                // Ajustement de la taille du texte en fonction du nombre de chiffres
                val digitCount = total.toInt().toString().length
                val textSize = when {
                    digitCount <= 3 -> 32f
                    digitCount == 4 -> 30f
                    digitCount == 5 -> 25f
                    digitCount == 6 -> 22f
                    else -> 20f
                }
                tvCounterMoney.textSize = textSize
            }
        }
    }

    @SuppressLint("NewApi")
    private fun fetchTransactions() {
        // Récupération des 3 dernières transactions depuis Firestore
        db.collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { result ->
                val transactions = mutableListOf<Transaction>()
                for (document in result) {
                    // Extraction des données de chaque transaction
                    val amount = document.getDouble("amount") ?: 0.0
                    val reason = document.getString("reason") ?: "Aucune raison"
                    val tag = document.getString("tag") ?: "Aucun tag"
                    val date = document.getDate("date") ?: Date()

                    transactions.add(Transaction(document.id, amount, reason, tag, date))
                }
                // Mise à jour de l'adaptateur avec les nouvelles transactions
                transactionAdapter.updateData(transactions)
            }
            .addOnFailureListener {
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
