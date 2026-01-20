package com.alert.app.fragment.auth
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alert.app.R
import com.alert.app.databinding.FragmentConfirmationBinding

class ConfirmationFragment : Fragment() {

    private lateinit var binding: FragmentConfirmationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvaccept.setOnClickListener { showAlertBox("Accepted!", "Invitation Accept Successfully.", R.drawable.tick_icon) }
        binding.tvreject.setOnClickListener { showAlertBox("Rejected!", "You have Rejected the Invitation.", R.drawable.group_627) }
    }

    @SuppressLint("SetTextI18n")
    private fun showAlertBox(header: String, message: String, imageResId: Int) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_accept_reject)
            setCancelable(false)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }

        dialog.findViewById<ImageView>(R.id.img_logo).setImageResource(imageResId)
        dialog.findViewById<TextView>(R.id.tv_header).text = header
        dialog.findViewById<TextView>(R.id.tv_text).text = message
        dialog.findViewById<TextView>(R.id.btn_ok).setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.knowMoreFragment2)
        }
        dialog.findViewById<ImageView>(R.id.img_close).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }



}
