package br.com.projeto_android

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.projeto_android.MainActivity.Companion.db
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import models.Pedido

class MenuClienteActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSolicitarOrcamento: MaterialButton
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var userNameTextView: TextView
    private val pedidosList = mutableListOf<Pedido>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_cliente)

        // Configurar a Toolbar
        toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        userNameTextView = findViewById(R.id.user_name)

        // Configurar RecyclerView e Button
        recyclerView = findViewById(R.id.recyclerViewPedidos)
        btnSolicitarOrcamento = findViewById(R.id.btnSolicitarOrcamento)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PedidosAdapter(pedidosList)

        btnSolicitarOrcamento.setOnClickListener {
            Toast.makeText(this, "Solicitando orçamento...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, OrcamentoActivity::class.java)
            startActivity(intent)
        }

        // Configurar BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> true
                R.id.menu_profile -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Abrindo perfil...", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.menu_logout -> {
                    Toast.makeText(this, "Saindo...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        val idEmpresa = getLoggedCompanyId()
        if (idEmpresa.isNotEmpty()) {
            fetchEmpresaNome(idEmpresa)
            fetchPedidos(idEmpresa)
        } else {
            Toast.makeText(this, "ID da empresa não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchEmpresaNome(idEmpresa: String) {
        db.collection("empresa")
            .document(idEmpresa)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nomeFantasia = document.getString("nomeFantasia")
                    userNameTextView.text = nomeFantasia ?: "Nome da Empresa"
                } else {
                    Toast.makeText(this, "Empresa não encontrada", Toast.LENGTH_SHORT).show()
                    userNameTextView.text = "Nome da Empresa"
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Erro ao buscar nome da empresa: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
                userNameTextView.text = "Nome da Empresa"
            }
    }

    private fun fetchPedidos(idEmpresa: String) {
        db.collection("pedido")
            .whereEqualTo("idEmpresa", idEmpresa)
            .get()
            .addOnSuccessListener { documents ->
                pedidosList.clear()
                for (document in documents) {
                    val pedido = document.toObject(Pedido::class.java).apply {
                        var id = document.id
                    }
                    pedidosList.add(pedido)
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Erro ao buscar pedidos: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun getLoggedCompanyId(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("id_empresa", "") ?: ""
    }

    class PedidosAdapter(private val pedidos: List<Pedido>) :
        RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_item_pedido, parent, false)
            return PedidoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
            val pedido = pedidos[position]
            holder.statusTextView.text = pedido.status
            holder.nomeTextView.text = pedido.nomeProjeto

            holder.chatButton.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, CommentsActivity::class.java)
                intent.putExtra("NOME_PEDIDO", pedido.nomeProjeto)
                intent.putExtra(
                    "ID_PEDIDO", pedido.id
                )
                context.startActivity(intent)
            }


            holder.avaliacaoButton.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, AvaliacaoActivity::class.java)
                intent.putExtra("NOME_PEDIDO", pedido.nomeProjeto)
                context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int = pedidos.size

        inner class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val statusTextView: TextView = itemView.findViewById(R.id.textViewStatus)
            val nomeTextView: TextView = itemView.findViewById(R.id.textViewNome)
            val chatButton: ImageButton = itemView.findViewById(R.id.buttonChat)
            val avaliacaoButton: ImageButton = itemView.findViewById(R.id.buttonAvaliacao)
        }
    }
}