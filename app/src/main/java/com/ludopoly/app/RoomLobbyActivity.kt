package com.ludopoly.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ludopoly.app.databinding.ActivityRoomLobbyBinding

class RoomLobbyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomLobbyBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var roomId: String = ""
    private var username: String = ""
    private var isHost: Boolean = false
    private var roomListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomLobbyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        roomId = intent.getStringExtra("EXTRA_ROOM_ID") ?: ""
        username = intent.getStringExtra("EXTRA_USERNAME") ?: (auth.currentUser?.displayName ?: "Pemain")
        isHost = intent.getBooleanExtra("EXTRA_IS_HOST", false)

        binding.tvLobbyRoomId.text = roomId

        if (roomId.isEmpty()) {
            Toast.makeText(this, "ID Room tidak valid.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupButtons()
        listenToRoomDetails()
    }

    private fun setupButtons() {
        binding.btnCopyRoomId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Kode Room", roomId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Kode Room $roomId disalin ke clipboard!", Toast.LENGTH_SHORT).show()
        }

        binding.btnLeaveRoom()

        if (isHost) {
            binding.btnStartGame.visibility = View.VISIBLE
            binding.tvWaitingHostNotice.visibility = View.GONE
            binding.btnStartGame.setOnClickListener {
                startGame()
            }
        } else {
            binding.btnStartGame.visibility = View.GONE
            binding.tvWaitingHostNotice.visibility = View.VISIBLE
        }
    }

    private fun ActivityRoomLobbyBinding.btnLeaveRoom() {
        btnLeaveRoom.setOnClickListener {
            showLeaveConfirmation()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun listenToRoomDetails() {
        roomListener = firestore.collection("rooms").document(roomId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Toast.makeText(this, "Error memuat room: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (document == null || !document.exists()) {
                    Toast.makeText(this, "Room telah ditutup oleh Host.", Toast.LENGTH_LONG).show()
                    finish()
                    return@addSnapshotListener
                }

                val status = document.getString("status") ?: "WAITING"
                val roomName = document.getString("roomName") ?: "Room LudoPoly"
                val players = document.get("players") as? List<Map<String, Any>> ?: listOf()
                val maxPlayers = document.getLong("maxPlayers") ?: 4
                val hostUid = document.getString("hostUid") ?: ""

                binding.tvLobbyRoomName.text = roomName
                binding.tvPlayerSlotsHeader.text = "PEMAIN DI DALAM ROOM (${players.size}/$maxPlayers)"

                // Jika permainan dimulai oleh Host, bawa semua pemain ke GameArenaActivity
                if (status == "PLAYING") {
                    roomListener?.remove()
                    val intent = Intent(this, GameArenaActivity::class.java)
                    intent.putExtra("EXTRA_ROOM_ID", roomId)
                    startActivity(intent)
                    finish()
                    return@addSnapshotListener
                }

                // Perbarui status Host apakah user saat ini adalah Host
                val currentUid = auth.currentUser?.uid
                if (currentUid != null && currentUid == hostUid) {
                    isHost = true
                    binding.btnStartGame.visibility = View.VISIBLE
                    binding.tvWaitingHostNotice.visibility = View.GONE
                }

                renderPlayerSlots(players, maxPlayers.toInt())
            }
    }

    private fun renderPlayerSlots(players: List<Map<String, Any>>, maxSlots: Int) {
        binding.containerPlayers.removeAllViews()

        // Render pemain yang sudah terhubung
        for (i in 0 until maxSlots) {
            val slotView = layoutInflater.inflate(R.layout.item_player_slot, binding.containerPlayers, false)

            val tvName = slotView.findViewById<TextView>(R.id.tvPlayerSlotName)
            val tvRole = slotView.findViewById<TextView>(R.id.tvPlayerRole)
            val tvStatusBadge = slotView.findViewById<TextView>(R.id.tvPlayerStatusBadge)
            val ivAvatar = slotView.findViewById<ImageView>(R.id.ivPlayerAvatar)

            if (i < players.size) {
                val playerMap = players[i]
                val playerName = playerMap["username"] as? String ?: "Pemain ${i + 1}"
                val playerIsHost = playerMap["isHost"] as? Boolean ?: false

                tvName.text = playerName
                tvName.setTextColor(ContextCompat.getColor(this, R.color.text_white))

                if (playerIsHost) {
                    tvRole.text = "👑 Host Ruangan"
                    tvRole.setTextColor(ContextCompat.getColor(this, R.color.gold_accent))
                } else {
                    tvRole.text = "🎮 Penantang"
                    tvRole.setTextColor(ContextCompat.getColor(this, R.color.purple_light))
                }

                tvStatusBadge.text = "SIAP"
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.color_success))
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge)
                ivAvatar.alpha = 1.0f
            } else {
                // Slot Kosong
                tvName.text = "Slot Kosong"
                tvName.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                tvRole.text = "Menunggu pemain masuk..."
                tvRole.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))

                tvStatusBadge.text = "KOSONG"
                tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                tvStatusBadge.setBackgroundResource(R.drawable.bg_input)
                ivAvatar.alpha = 0.3f
            }

            binding.containerPlayers.addView(slotView)
        }
    }

    private fun startGame() {
        binding.btnStartGame.isEnabled = false
        binding.btnStartGame.text = "Memulai Permainan..."

        firestore.collection("rooms").document(roomId)
            .update("status", "PLAYING")
            .addOnSuccessListener {
                // Listener akan otomatis memindahkan activity ke GameArenaActivity
            }
            .addOnFailureListener { e ->
                binding.btnStartGame.isEnabled = true
                binding.btnStartGame.text = "🎮  MULAI PERMAINAN"
                Toast.makeText(this, "Gagal memulai: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLeaveConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Room")
            .setMessage("Apakah Anda yakin ingin keluar dari room ini?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                leaveRoomProcess()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    @Suppress("UNCHECKED_CAST")
    private fun leaveRoomProcess() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("rooms").document(roomId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val hostUid = doc.getString("hostUid")
                    if (hostUid == uid) {
                        // Jika Host keluar, hapus room dari Firestore
                        firestore.collection("rooms").document(roomId).delete()
                    } else {
                        // Jika pemain biasa keluar, hapus dari daftar pemain
                        val players = doc.get("players") as? List<Map<String, Any>> ?: listOf()
                        val updatedPlayers = players.filter { it["uid"] != uid }
                        firestore.collection("rooms").document(roomId)
                            .update("players", updatedPlayers)
                    }
                }
                finish()
            }
            .addOnFailureListener {
                finish()
            }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showLeaveConfirmation()
    }

    override fun onDestroy() {
        super.onDestroy()
        roomListener?.remove()
    }
}
