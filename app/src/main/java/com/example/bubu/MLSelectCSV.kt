package com.example.bubu


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.Toast
import com.example.bubu.GraphPlots.MlGraphActivity


class MLSelectCSV : AppCompatActivity() {

    private val fileRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_csv)

        val selectFileButton: Button = findViewById(R.id.button_select_file)

        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, fileRequestCode)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == fileRequestCode && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileName(uri)
                Toast.makeText(this, fileName, Toast.LENGTH_LONG).show()

                val intent = Intent(this, MlGraphActivity::class.java)
                intent.putExtra("URI", uri.toString())
                startActivity(intent)

            }
        }
    }

    fun getFileName(uri: Uri): String {
        var name = ""

        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex)
                } else {
                    // fallback if DISPLAY_NAME not found
                    name = uri.lastPathSegment ?: "unknown_file"
                }
            }
        }

        return name
    }

}
