package com.example.android.openweather

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.preference.DialogPreference
import androidx.preference.Preference

class NumberPickerPreference : DialogPreference {
    // the values to use for the NumberPicker
    var value = 0
        set(value) {
            val wasBlocking = shouldDisableDependents()
            field = value
            persistInt(value)
            val isBlocking = shouldDisableDependents()
            if (isBlocking != wasBlocking) {
                notifyDependencyChange(isBlocking)
            }
        }
    var defaultValue = 5
    var maxValue = 5
    var minValue = 1
    var selectorWheelValue = false // enable or disable the 'circular behavior' - set on false

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0, 0)
    }

    constructor(context: Context) : super(context) {
        init(context, null, 0, 0)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val ta = context.obtainStyledAttributes(
            attrs, R.styleable.Number_Picker_attrs, defStyleAttr, defStyleRes
        )
        minValue = ta.getInt(R.styleable.Number_Picker_attrs_np_minValue, minValue)
        maxValue = ta.getInt(R.styleable.Number_Picker_attrs_np_maxValue, maxValue)
        defaultValue = ta.getInt(R.styleable.Number_Picker_attrs_np_defaultValue, defaultValue)
        selectorWheelValue = ta.getBoolean(R.styleable.Number_Picker_attrs_np_wrapSelector, selectorWheelValue)
        ta.recycle()
        dialogLayoutResource = R.layout.number_picker_layout
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInt(index, defaultValue)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = this.defaultValue
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        val myState = SavedState(superState)
        myState.value = value
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            super.onRestoreInstanceState(state)
            return
        }

        val myState = state as SavedState?
        super.onRestoreInstanceState(myState!!.getSuperState())
        value = myState.value
    }

    private class SavedState : Preference.BaseSavedState, Parcelable {
        override fun describeContents(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        internal var value: Int = 0

        constructor(source: Parcel) : super(source) {
            value = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState) {}

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(value)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}