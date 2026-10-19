package com.ludopoly.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.ludopoly.app.databinding.ActivityRoomListBinding
import java.util.Random

class RoomListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomListBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var roomListener: ListenerRegistration? = null
    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnOpenCreateRoom.setOnClickListener {
            showCreateRoomDialog()
        }

        binding.btnOpenJoinRoom.setOnClickListener {
            showJoinRoomDialog()
        }

        binding.btnRefresh.setOnClickListener {
            Toast.makeText(this, "Memperbarui daftar room...", Toast.LENGTH_SHORT).show()
        }

        listenToActiveRooms()
    }

    private fun generateShortRoomId(): String {
        // Menghasilkan ID singkat 5 karakter yang mudah diketik (contoh: LP482, LP79X)
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val sb = StringBuilder("LP")
        for (i in 0 until 3) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }

    private fun listenToActiveRooms() {
        binding.progressBar.visibility = View.VISIBLE

        roomListener = firestore.collection("rooms")
            .whereEqualTo("status", "WAITING")
            .addSnapshotListener { snapshots, error ->
                binding.progressBar.visibility = View.GONE

                if (error != null) {
                    binding.tvActiveRoomsCount.text = "Error: ${error.localizedMessage}"
                    return@addSnapshotListener
                }

                if (snapshots == null || snapshots.isEmpty) {
                    binding.containerRooms.removeAllViews()
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.tvActiveRoomsCount.text = "0 Room Aktif"
                    return@addSnapshotListener
                }

                binding.layoutEmptyState.visibility = View.GONE
                binding.tvActiveRoomsCount.text = "${snapshots.size()} Room Aktif Tersedia"

                // Urutkan room terbaru di memori tanpa perlu composite index Firestore
                val sortedDocs = snapshots.documents.sortedByDescending { it.getLong("createdAt") ?: 0L }
                renderRoomCards(sortedDocs)
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun renderRoomCards(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        binding.containerRooms.removeAllViews()

        for (doc in documents) {
            val roomId = doc.getString("roomId") ?: doc.id
            val roomName = doc.getString("roomName") ?: "Room LudoPoly"
            val hostName = doc.getString("hostName") ?: "Pemain"
            val players = doc.get("players") as? List<Map<String, Any>> ?: listOf()
            val maxPlayers = doc.getLong("maxPlayers") ?: 4

            val card = layoutInflater.inflate(R.layout.item_room_card, binding.containerRooms, false)

            val tvRoomName = card.findViewById<TextView>(R.id.tvRoomName)
            val tvHostName = card.findViewById<TextView>(R.id.tvHostName)
            val tvRoomIdBadge = card.findViewById<TextView>(R.id.tvRoomIdBadge)
            val tvPlayerCount = card.findViewById<TextView>(R.id.tvPlayerCount)
            val btnJoin = card.findViewById<AppCompatButton>(R.id.btnJoinThisRoom)
            val btnCopy = card.findViewById<View>(R.id.btnCopyId)

            tvRoomName.text = roomName
            tvHostName.text = "Host: $hostName"
            tvRoomIdBadge.text = "ID: $roomId"
            tvPlayerCount.text = "${players.size}/$maxPlayers Pemain"

            btnCopy.setOnClickListener {
                copyToClipboard(roomId)
            }

            if (players.size >= maxPlayers) {
                btnJoin.text = "PENUH"
                btnJoin.isEnabled = false
                btnJoin.alpha = 0.5f
            } else {
                btnJoin.text = "GABUNG ➔"
                btnJoin.isEnabled = true
                btnJoin.alpha = 1.0f

                btnJoin.setOnClickListener {
                    promptJoinSelectedRoom(roomId, roomName)
                }
            }

            binding.containerRooms.addView(card)
        }
    }

    private fun showCreateRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val etRoomName = dialogView.findViewById<TextInputEditText>(R.id.etDialogRoomName)
        val tilRoomName = dialogView.findViewById<TextInputLayout>(R.id.tilDialogRoomName)
        val tvGeneratedId = dialogView.findViewById<TextView>(R.id.tvGeneratedRoomId)
        val btnRegenerate = dialogView.findViewById<AppCompatButton>(R.id.btnRegenerateId)
        val etHostUsername = dialogView.findViewById<TextInputEditText>(R.id.etDialogHostUsername)
        val tilHostUsername = dialogView.findViewById<TextInputLayout>(R.id.tilDialogHostUsername)
        val btnSubmit = dialogView.findViewById<AppCompatButton>(R.id.btnSubmitCreateRoom)

        var generatedId = generateShortRoomId()
        tvGeneratedId.text = generatedId

        btnRegenerate.setOnClickListener {
            generatedId = generateShortRoomId()
            tvGeneratedId.text = generatedId
        }

        // Prefill nama user saat ini
        val defaultUsername = auth.currentUser?.displayName ?: "Pemain"
        etHostUsername.setText(defaultUsername)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSubmit.setOnClickListener {
            val roomName = etRoomName.text.toString().trim()
            val hostUsername = etHostUsername.text.toString().trim()

            if (roomName.isEmpty()) {
                tilRoomName.error = "Nama room wajib diisi"
                return@setOnClickListener
            } else {
                tilRoomName.error = null
            }

            if (hostUsername.isEmpty()) {
                tilHostUsername.error = "Username wajib diisi"
                return@setOnClickListener
            } else {
                tilHostUsername.error = null
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Membuat Room..."

            val uid = auth.currentUser?.uid ?: "guest_${System.currentTimeMillis()}"

            val initialPlayer = hashMapOf(
                "uid" to uid,
                "username" to hostUsername,
                "isHost" to true
            )

            val roomData = hashMapOf(
                "roomId" to generatedId,
                "roomName" to roomName,
                "hostUid" to uid,
                "hostName" to hostUsername,
                "maxPlayers" to 4,
                "status" to "WAITING",
                "createdAt" to System.currentTimeMillis(),
                "players" to listOf(initialPlayer)
            )

            firestore.collection("rooms").document(generatedId)
                .set(roomData)
                .addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(this, "Room $generatedId berhasil dibuat!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, RoomLobbyActivity::class.java)
                    intent.putExtra("EXTRA_ROOM_ID", generatedId)
                    intent.putExtra("EXTRA_USERNAME", hostUsername)
                    intent.putExtra("EXTRA_IS_HOST", true)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "BUAT & MASUK ROOM"
                    Toast.makeText(this, "Gagal membuat room: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        dialog.show()
    }

    private fun showJoinRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_join_room, null)
        val etUsername = dialogView.findViewById<TextInputEditText>(R.id.etJoinUsername)
        val tilUsername = dialogView.findViewById<TextInputLayout>(R.id.tilJoinUsername)
        val etRoomId = dialogView.findViewById<TextInputEditText>(R.id.etJoinRoomId)
        val tilRoomId = dialogView.findViewById<TextInputLayout>(R.id.tilJoinRoomId)
        val btnSubmit = dialogView.findViewById<AppCompatButton>(R.id.btnSubmitJoinRoom)

        val defaultUsername = auth.currentUser?.displayName ?: "Pemain"
        etUsername.setText(defaultUsername)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSubmit.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val inputRoomId = etRoomId.text.toString().trim().uppercase()

            if (username.isEmpty()) {
                tilUsername.error = "Username wajib diisi"
                return@setOnClickListener
            } else {
                tilUsername.error = null
            }

            if (inputRoomId.isEmpty()) {
                tilRoomId.error = "Kode Room ID wajib diisi"
                return@setOnClickListener
            } else {
                tilRoomId.error = null
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Memeriksa Room..."

            joinRoomProcess(inputRoomId, username, dialog, btnSubmit)
        }

        dialog.show()
    }

    private fun promptJoinSelectedRoom(roomId: String, roomName: String) {
        val defaultUsername = auth.currentUser?.displayName ?: "Pemain"

        val input = TextInputEditText(this)
        input.setText(defaultUsername)
        input.setHint("Masukkan Username Anda")

        AlertDialog.Builder(this)
            .setTitle("Gabung ke $roomName")
            .setMessage("Masukkan nama pemain Anda untuk bergabung ke Room ID [$roomId]:")
            .setView(input)
            .setPositiveButton("Gabung") { _, _ ->
                val username = input.text.toString().trim().ifEmpty { defaultUsername }
                joinRoomProcess(roomId, username, null, null)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    @Suppress("UNCHECKED_CAST")
    private fun joinRoomProcess(roomId: String, username: String, dialogToDismiss: AlertDialog?, submitBtn: AppCompatButton?) {
        val uid = auth.currentUser?.uid ?: "player_${System.currentTimeMillis()}"

        firestore.collection("rooms").document(roomId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    submitBtn?.isEnabled = true
                    submitBtn?.text = "GABUNG KE ROOM"
                    Toast.makeText(this, "Room ID [$roomId] tidak ditemukan! Periksa kembali kodenya.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val status = document.getString("status") ?: "WAITING"
                if (status != "WAITING") {
                    submitBtn?.isEnabled = true
                    submitBtn?.text = "GABUNG KE ROOM"
                    Toast.makeText(this, "Permainan di room ini sudah berlangsung atau selesai.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val players = document.get("players") as? List<Map<String, Any>> ?: listOf()
                val maxPlayers = document.getLong("maxPlayers") ?: 4

                // Cek apakah pemain sudah ada di room
                val alreadyInRoom = players.any { it["uid"] == uid }
                if (!alreadyInRoom && players.size >= maxPlayers) {
                    submitBtn?.isEnabled = true
                    submitBtn?.text = "GABUNG KE ROOM"
                    Toast.makeText(this, "Room ini sudah penuh ($maxPlayers pemain).", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val newPlayer = hashMapOf(
                    "uid" to uid,
                    "username" to username,
                    "isHost" to false
                )

                // Tambahkan pemain ke dalam room di Firestore
                firestore.collection("rooms").document(roomId)
                    .update("players", FieldValue.arrayUnion(newPlayer))
                    .addOnSuccessListener {
                        dialogToDismiss?.dismiss()
                        Toast.makeText(this, "Berhasil masuk ke Room $roomId!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, RoomLobbyActivity::class.java)
                        intent.putExtra("EXTRA_ROOM_ID", roomId)
                        intent.putExtra("EXTRA_USERNAME", username)
                        intent.putExtra("EXTRA_IS_HOST", false)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        submitBtn?.isEnabled = true
                        submitBtn?.text = "GABUNG KE ROOM"
                        Toast.makeText(this, "Gagal bergabung: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                submitBtn?.isEnabled = true
                submitBtn?.text = "GABUNG KE ROOM"
                Toast.makeText(this, "Koneksi bermasalah: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Room ID", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Kode Room $text berhasil disalin!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
    }
}
