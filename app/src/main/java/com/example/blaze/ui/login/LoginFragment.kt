package com.example.blaze.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.blaze.R
import com.example.blaze.databinding.FragmentLoginBinding
import com.example.blaze.models.BlazeUser
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentLoginBinding.bind(view)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        auth = Firebase.auth

        binding.apply{
            signInBtn.setOnClickListener {
                signIn()
            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                startLoading()
                GlobalScope.launch(Dispatchers.IO){
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Snackbar.make(requireActivity().findViewById(R.id.navHostFragment), "Login Failed! Please check your internet connection", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        GlobalScope.launch(Dispatchers.IO){
                            withContext(Dispatchers.Main) {
                                val user = auth.currentUser
                                updateUI(user)
                            }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                            GlobalScope.launch{
                                withContext(Dispatchers.Main){
                                    stopLoading()
                                    updateUI(null)
                                    Snackbar.make(requireActivity().findViewById(R.id.navHostFragment), "Login Failed! Please check your internet connection", Snackbar.LENGTH_LONG).show()
                                }
                            }
                    }
                }
    }

    private fun updateUI(firebaseUser: FirebaseUser?){
        if(firebaseUser != null) {
            val user = BlazeUser(firebaseUser.uid, firebaseUser.email!!.toString(),
                    firebaseUser.displayName!!.toString(), firebaseUser.photoUrl!!.toString())

            Snackbar.make(requireActivity().findViewById(R.id.navHostFragment), "Login Successful!", Snackbar.LENGTH_LONG).show()
            val action = LoginFragmentDirections.actionLoginFragmentToChannelFragment(user)
            findNavController().navigate(action)
        }
        else{
            stopLoading()
        }
    }

    private fun startLoading(){
        binding.signInBtn.visibility = View.INVISIBLE
        binding.signInLoadingText.visibility = View.VISIBLE
        binding.loginLoadingBar.visibility = View.VISIBLE
    }

    private fun stopLoading(){
        binding.signInBtn.visibility = View.VISIBLE
        binding.signInLoadingText.visibility = View.INVISIBLE
        binding.loginLoadingBar.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}