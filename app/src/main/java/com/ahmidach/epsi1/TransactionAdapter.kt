package com.ahmidach.epsi1

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.content.Context
import com.ahmidach.epsi1.Utils.Companion.toStringCustom
import com.google.firebase.firestore.FirebaseFirestore

// Déclaration de la classe TransactionAdapter, un adapter pour afficher une liste de transactions dans un RecyclerView
class TransactionAdapter(private var transactions: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    // Instance de FirebaseFirestore pour interagir avec la base de données Firestore
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Vue détentrice des éléments UI pour chaque transaction dans une ligne du RecyclerView
    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)
        val tvReason: TextView = itemView.findViewById(R.id.tv_reason)
        val tvTag: TextView = itemView.findViewById(R.id.tv_tag)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val parentView = itemView
    }

    // Fonction pour créer une nouvelle vue pour chaque élément du RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    // Fonction pour lier les données d'une transaction à la vue correspondante dans le RecyclerView
    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvAmount.text = (if (transaction.amount >= 0) "+" else "") + String.format("%.2f€", transaction.amount)
        val color = if (transaction.amount > 0) Color.parseColor("#4CAF50") else Color.RED
        holder.tvAmount.setTextColor(color)
        holder.tvReason.text = transaction.reason

        // Gestion de la visibilité du tag selon sa présence
        if (transaction.tag.isBlank()) {
            holder.tvTag.visibility = View.GONE
        } else {
            holder.tvTag.visibility = View.VISIBLE
        }

        // Gestion de la visibilité de la raison selon sa présence
        if (transaction.reason.isBlank()) {
            holder.tvReason.visibility = View.GONE
        } else {
            holder.tvTag.visibility = View.VISIBLE
        }

        holder.tvDate.text = transaction.date.toStringCustom()

        // Ajout d'un écouteur de clic pour afficher une popup de sélection de tag
        holder.parentView.setOnClickListener {
            showTagsPopup(holder.parentView.context, transaction)
        }
    }

    // Fonction pour afficher une popup permettant de choisir un tag pour la transaction
    private fun showTagsPopup(context: Context, transaction: Transaction) {
        val tags = arrayOf("Voiture", "Maison", "Plaisirs")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sélectionner un tag")
        builder.setItems(tags) { dialog, which ->
            val selectedTag = tags[which]
            saveTransactionToFirestore(selectedTag, transaction)
        }
        builder.show()
    }

    // Fonction pour enregistrer la transaction dans Firestore avec un tag spécifique
    private fun saveTransactionToFirestore(tag: String, transaction: Transaction) {
        val transactionData = hashMapOf(
            "amount" to transaction.amount,
            "reason" to transaction.reason,
            "tag" to transaction.tag,
            "date" to transaction.date
        )

        // Enregistrement des données de la transaction sous le tag choisi dans Firestore
        db.collection(tag)
            .add(transactionData)
            .addOnSuccessListener { documentReference ->
                // Message de succès si l'enregistrement dans Firestore est réussi
                println("Transaction enregistrée avec l'ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Message d'erreur si l'enregistrement échoue
                println("Erreur lors de l'enregistrement de la transaction: $e")
            }
    }

    // Fonction pour obtenir le nombre d'éléments dans la liste de transactions
    override fun getItemCount(): Int = transactions.size

    // Fonction pour mettre à jour les données du RecyclerView avec une nouvelle liste de transactions
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}

