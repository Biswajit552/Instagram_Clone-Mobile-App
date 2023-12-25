package com.example.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.instagram.Models.User
import com.example.instagram.databinding.ActivitySignupBinding
import com.example.instagram.utils.USER_NODE
import com.example.instagram.utils.USER_PROFILE_FOLDER
import com.example.instagram.utils.uploadImages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
class signupActivity : AppCompatActivity() {
    val binding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    lateinit var user: User

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImages(uri, USER_PROFILE_FOLDER) {
                if(it != null) {
                    user.image = it
                    binding.profileImage.setImageURI(uri)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val text =
            "<font color=#000000>Already have an Account ?</font> <font color=#0B37B8>Login</font>"
        binding.login.setText(Html.fromHtml(text))
        user = User()
        if (intent.hasExtra("MODE")) {
            if (intent.getIntExtra("MODE", -1) == 1) {
                binding.signUpBtn.text = "Update Profile"
                Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser!!.uid)
                    .get().addOnSuccessListener {
                        user = it.toObject<User>()!!
                        if (!user.image.isNullOrEmpty()) {
                            Picasso.get().load(user.image).into(binding.profileImage)
                        }
                        binding.name.editText?.setText(user.name)
                        binding.email.editText?.setText(user.email)
                        binding.password.editText?.setText(user.password)
                    }
            }
        }

            binding.signUpBtn.setOnClickListener {
                if (intent.hasExtra("MODE")){
                    if (intent.getIntExtra("MODE",-1)==1){
                        Firebase.firestore.collection(USER_NODE)
                            .document(Firebase.auth.currentUser!!.uid).set(user)
                            .addOnSuccessListener {
                                startActivity(
                                    Intent(
                                        this@signupActivity,
                                        HomeActivity::class.java
                                    )
                                )
                                finish()
                            }
                    }
                }else {
                    if (binding.name.editText?.text.toString().equals("") or
                        binding.email.editText?.text.toString().equals("") or
                        binding.password.editText?.text.toString().equals("")
                    ) {
                        Toast.makeText(
                            this@signupActivity,
                            "please fill the all info",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                            binding.email.editText?.text.toString(),
                            binding.password.editText?.text.toString()
                        ).addOnCompleteListener { result ->
                            if (result.isSuccessful) {
                                user.name = binding.name.editText?.text.toString()
                                user.email = binding.email.editText?.text.toString()
                                user.password = binding.password.editText?.text.toString()
                                Firebase.firestore.collection(USER_NODE)
                                    .document(Firebase.auth.currentUser!!.uid).set(user)
                                    .addOnSuccessListener {
                                        startActivity(
                                            Intent(
                                                this@signupActivity,
                                                HomeActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                            } else {
                                Toast.makeText(
                                    this@signupActivity,
                                    result.exception?.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            binding.addimage.setOnClickListener {
                launcher.launch("image/*")
            }
            binding.login.setOnClickListener {
                startActivity(Intent(this@signupActivity, loginActivity::class.java))
                finish()
            }

    }
}