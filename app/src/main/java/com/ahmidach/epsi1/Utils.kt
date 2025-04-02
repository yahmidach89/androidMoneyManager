package com.ahmidach.epsi1

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Définition de la classe Utils contenant une fonction utilitaire pour formater les dates
class Utils {
    companion object {
        // Extension de la classe Date pour ajouter une méthode 'toStringCustom' qui formate la date
        fun Date.toStringCustom(): String {
            // Création d'un objet SimpleDateFormat pour formater la date en "jour/mois/année heure:minute"
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE)
            // Retourne la date formatée en utilisant le format spécifié
            return dateFormat.format(this)
        }
    }
}
