package com.umg.mysportzac

import java.util.regex.Pattern
import java.util.regex.Matcher
class ValidateEmail {
    companion object{
        var pat: Pattern?= null
        var mat: Matcher?= null

        fun isEmail(email:String): Boolean{
            pat = Pattern.compile("^[\\w\\-\\_\\+]+(\\.[\\w\\-\\_]+)*@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,4}$")
            mat = pat!!.matcher(email)
            return mat!!.find()
        }
    }
}