package arunkbabu.care.fragments.signup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import arunkbabu.care.R
import arunkbabu.care.Utils
import com.google.android.gms.tasks.Task
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.SignInMethodQueryResult
import kotlinx.android.synthetic.main.fragment_sign_up_doctor.*

class SignUpDoctorFragment : Fragment(), View.OnFocusChangeListener {
    companion object {
        @JvmField
        var signUpDoctorFragActive = false
    }

    private lateinit var mAuth: FirebaseAuth
    private var mIsEmailRegistered = false
    private var mIsFragmentDetached = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mIsFragmentDetached = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_doctor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signUpDoctorFragActive = true

        mAuth = FirebaseAuth.getInstance()

        et_doc_sign_up_email.onFocusChangeListener = this
        et_doc_sign_up_password.onFocusChangeListener = this
        et_doc_sign_up_password_confirm.onFocusChangeListener = this
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        when (v.id) {
            R.id.et_sign_up_email ->
                if (!hasFocus) {
                    // Only check the validity of the email, if the user navigates away from the view
                    checkEmail()
                }
            R.id.et_sign_up_password ->
                if (!hasFocus) {
                    // Only check the password, if the user navigates away from the view
                    checkPassword(SignUpPatientFragment.MODE_FIELD_1)
                }
            R.id.et_sign_up_password_confirm ->
                if (!hasFocus) {
                    // Only check the password, if the user navigates away from the view
                    checkPassword(SignUpPatientFragment.MODE_FIELD_2)
                }
        }
    }

    /**
     * Check whether the email is valid or not empty
     * @return True If it is valid
     */
    private fun checkEmail(): Boolean {
        val email = getEmail()
        when {
            email.isBlank() -> {
                // Email is blank
                et_doc_sign_up_email.error = getString(R.string.err_blank_email)
                return false
            }
            Utils.verifyEmail(email) -> {
                // Invalid Email
                et_doc_sign_up_email.error = getString(R.string.err_invalid_email)
                return false
            }
            else -> {
                checkIsEmailRegistered(email)
            }
        }
        // Email is correct
        return true
    }

    /**
     * Checks whether the password is not empty
     * @param mode The evaluation mode; whether to check all the fields or or just a single one
     * @return True If it is not empty
     */
    private fun checkPassword(mode: Int): Boolean {
        val password1: String = et_doc_sign_up_password.text.toString()
        val password2: String = et_doc_sign_up_password_confirm.text.toString()
        when (mode) {
            SignUpPatientFragment.MODE_FIELD_1 ->
                if (password1.isBlank()) {
                    // Password field is empty
                    et_doc_sign_up_password.error = getString(R.string.err_sign_up_password)
                    return false
                } else if (password1.length < 8) {
                    et_doc_sign_up_password.error = getString(R.string.err_password_length)
                    return false
                }
            SignUpPatientFragment.MODE_FIELD_2 ->
                if (password1 != password2) {
                    // Passwords doesn't match
                    et_doc_sign_up_password_confirm.error = getString(R.string.err_password_no_match)
                    return false
                }
            SignUpPatientFragment.MODE_BOTH ->
                when {
                    password1.isBlank() -> {
                        // Password field is empty
                        et_doc_sign_up_password.error = getString(R.string.err_sign_up_password)
                        return false
                    }
                    password1.length < 8 -> {
                        et_doc_sign_up_password.error = getString(R.string.err_password_length)
                        return false
                    }
                    password1 != password2 -> {
                        // Passwords doesn't match
                        et_doc_sign_up_password_confirm.error = getString(R.string.err_password_no_match)
                        return false
                    }
                }
        }
        // Password is correct
        return true
    }

    /**
     * Helper method to check if the email is already registered
     * @param email The email to check against
     */
    private fun checkIsEmailRegistered(email: String) {
        if (!mIsFragmentDetached) {
            mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task: Task<SignInMethodQueryResult> ->
                    val isRegistered = task.result.signInMethods?.isNotEmpty() ?: false
                    if (isRegistered) {
                        // Email Registered
                        mIsEmailRegistered = true
                        et_doc_sign_up_email.error = getString(R.string.err_email_exists)
                    } else {
                        // Email NOT Registered
                        mIsEmailRegistered = false
                    }
                }
        }
    }

    /**
     * Returns the text in the First Name field
     * @return String  Text in the first name field
     */
    fun getFullName(): String = if (et_doc_sign_up_full_name.text.toString() == "null") "" else et_doc_sign_up_full_name.text.toString().trim()

    /**
     * Returns the text in the Email field
     * @return String  Text in the Email field
     */
    fun getEmail(): String = if (et_doc_sign_up_email.text.toString() == "null") "" else et_doc_sign_up_email.text.toString().trim()

    /**
     * Returns the text in the Password field
     * @return String  Text in the Password field
     */
    fun getPassword(): String = if (et_doc_sign_up_password.text.toString() == "null") "" else et_doc_sign_up_password.text.toString().trim()

    /**
     * Returns the text in the Mobile Number field
     * @return String  Text in the Mobile Number field
     */
    fun getMobileNumber(): String = if (et_doc_sign_up_mobile.text.toString() == "null") "" else et_doc_sign_up_mobile.text.toString().trim()

    /**
     * Returns the text in the Register Id field
     * @return String  Text in the Register Id field
     */
    fun getRegisterId(): String = if (et_doc_sign_up_reg_id.text.toString() == "null") "" else et_doc_sign_up_reg_id.text.toString().trim()

    /**
     * Inspects all the EditTexts in this fragment to check whether all are filled
     * @return True  if all the fields are filled and the emails and passwords are valid; False  otherwise
     */
    fun checkAllFields(): Boolean {
        val checkEmail = checkEmail()
        val checkPassword = checkPassword(SignUpPatientFragment.MODE_BOTH)

//        if (getFullName().isNotBlank() && checkEmail && checkPassword && getMobileNumber().isNotBlank()
//            && !mIsEmailRegistered && getRegisterId().isNotBlank()) {
//            // If all the fields are filled and emails and passwords are valid; Return true
//            return true
//        }

        if (getFullName().isNotBlank() && checkEmail && checkPassword && !mIsEmailRegistered && getRegisterId().isNotBlank()) {
            // If all the fields are filled and emails and passwords are valid; Return true
            return true
        }

        // Show an error in all the fields that are empty
        if (getFullName().isBlank())
            et_doc_sign_up_full_name.error = getString(R.string.err_empty_field)

//        if (getMobileNumber().isBlank())
//            et_doc_sign_up_mobile.error = getString(R.string.err_empty_field)

        if (getRegisterId().isBlank())
            et_doc_sign_up_reg_id.error = getString(R.string.err_empty_field)

        return false
    }

    override fun onStop() {
        super.onStop()
        mIsFragmentDetached = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        signUpDoctorFragActive = false
    }
}