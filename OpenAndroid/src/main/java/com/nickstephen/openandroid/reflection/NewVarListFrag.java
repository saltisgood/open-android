package com.nickstephen.openandroid.reflection;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nickstephen.lib.gui.FragmentUtil;
import com.nickstephen.lib.gui.ListFragment;
import com.nickstephen.lib.misc.StatMethods;
import com.nickstephen.openandroid.OpenAndroid;
import com.nickstephen.openandroid.R;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.AlertDialog;
import org.holoeverywhere.widget.ArrayAdapter;
import org.holoeverywhere.widget.CheckBox;
import org.holoeverywhere.widget.EditText;
import org.holoeverywhere.widget.NumberPicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Nick on 14/01/14.
 */
public class NewVarListFrag extends ListFragment {
    public static final String FRAG_TAG = "com.nickstephen.openandroid.reflection.NewVarListFrag";
    public static final String KEY_IS_PRIMITIVE = "is_primitive";

    private EditText mVarNameEditor;
    private EditText mDialogValueEditor;
    private RadioGroup mDialogTFGroup;
    private CheckBox mArrayCheck;
    private boolean mIsPrimitive = false;
    private OpenAndroid.Globals mGlobals;
    private int mSelectedItemPosition = AdapterView.INVALID_POSITION;
    private NumberPicker mArraySizePicker;
    private int mArrayPositionCount;
    private Object mReflectionObject;
    private Constructor<?> mReflectionCtor;

    private String mVarName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = this.getArguments();
        if (args != null) {
            mIsPrimitive = args.getBoolean(KEY_IS_PRIMITIVE, false);
        }

        if (mGlobals == null) {
            mGlobals = ((OpenAndroid) this.getSupportApplication()).getGlobals();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.newvar_listfrag);

        mVarNameEditor = (EditText) rootView.findViewById(R.id.var_name_edittext);

        mArrayCheck = (CheckBox) rootView.findViewById(R.id.var_is_array_check);

        View button = rootView.findViewById(R.id.make_var_button);
        button.setOnClickListener(mMakeVarButtonClick);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] array;
        if (mIsPrimitive) {
            array = this.getResources().getStringArray(R.array.primitives_array);
        } else {
            array = this.getResources().getStringArray(R.array.objects_array);
        }

        this.getListView().setAdapter(new ArrayAdapter<String>(this.getActivity(),
                android.R.layout.simple_list_item_single_choice, array));
        this.getListView().setOnItemClickListener(mListItemClickL);

        this.getSupportActionBar().setTitle("New Variable");
    }

    /**
     * Get the size of the array requested by the user. Only valid after the dialog has been dismissed
     * positively.
     * @return
     */
    private int getArraySize() {
        if (mArraySizePicker == null || !mArrayCheck.isChecked()) {
            return -1;
        } else {
            return mArraySizePicker.getValue() + 1;
        }
    }

    private void addToGlobals() {
        mGlobals.addObject(mVarName, mReflectionObject);
    }

    private final View.OnClickListener mMakeVarButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            {
                Editable editable = mVarNameEditor.getText();
                if (editable == null) {
                    return;
                }
                mVarName = editable.toString();
                if (StatMethods.IsStringNullOrEmpty(mVarName)) {
                    StatMethods.hotBread(NewVarListFrag.this.getActivity(), "Please enter a variable name",
                            Toast.LENGTH_SHORT);
                    return;
                }
            }

            if (!mGlobals.checkNameIsUnique(mVarName)) {
                StatMethods.hotBread(NewVarListFrag.this.getActivity(),
                        "Variable name not unique. Try again", Toast.LENGTH_SHORT);
                return;
            }

            if (mSelectedItemPosition == AdapterView.INVALID_POSITION) {
                StatMethods.hotBread(NewVarListFrag.this.getActivity(), "Please select a type", Toast.LENGTH_SHORT);
                return;
            }

            if (mArrayCheck.isChecked()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewVarListFrag.this.getActivity());
                mArraySizePicker = new NumberPicker(NewVarListFrag.this.getActivity());
                mArraySizePicker.setDisplayedValues(NewVarListFrag.this.getResources().getStringArray(R.array.number_picker_array));
                mArraySizePicker.setMinValue(0);
                mArraySizePicker.setMaxValue(19);
                mArraySizePicker.setWrapSelectorWheel(false);
                builder.setTitle("Choose array size").setView(mArraySizePicker)
                        .setPositiveButton("OK", mDialogOkL).setNegativeButton("Cancel", mDialogCancelL)
                        .setOnCancelListener(mDialogOnCancelL)
                        .show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewVarListFrag.this.getActivity());
                builder.setTitle("Enter a value").setView(getDialogView(null))
                        .setPositiveButton("OK", mDialogOkFinalVarL)
                        .setNegativeButton("Cancel", mDialogCancelL)
                        .setOnCancelListener(mDialogOnCancelL)
                        .show();
            }
        }
    };

    private final DialogInterface.OnClickListener mDialogOkFinalVarL = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (!mIsPrimitive || mSelectedItemPosition != 6) {
                setVariableValue(-1, mDialogValueEditor.getText().toString());
            } else if (mDialogTFGroup.getCheckedRadioButtonId() == R.id.radio_true) {
                setVariableValue(-1, "true");
            } else {
                setVariableValue(-1, "false");
            }
            addToGlobals();

            NewVarListFrag.this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,
                            R.anim.push_right_in, R.anim.push_right_out)
                    .replace(FragmentUtil.getContentViewCompat(), new VarListFrag(), VarListFrag.FRAG_TAG)
                    .addToBackStack(VarListFrag.FRAG_TAG).commit();
        }
    };

    private boolean setVariableValue(int position, @NotNull String value) {
        try {
            if (position == -1) {
                mReflectionObject = mReflectionCtor.newInstance(getGenericValue(value));
            } else {
                Array.set(mReflectionObject, position, mReflectionCtor.newInstance(getGenericValue(value)));
            }
            return true;
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable private Object errorMessage() {
        StatMethods.hotBread(this.getActivity(), "Could not parse input", Toast.LENGTH_SHORT);
        return null;
    }

    private View getDialogView(@Nullable String hint) {
        if (mIsPrimitive) {
            switch (mSelectedItemPosition) {
                case 6:
                    mDialogTFGroup = (RadioGroup) NewVarListFrag.this.getLayoutInflater().inflate(R.layout.var_tf_radio);
                    return mDialogTFGroup;
                case 7:
                    mDialogValueEditor = new EditText(NewVarListFrag.this.getActivity());
                    if (hint != null) {
                        mDialogValueEditor.setHint(hint);
                    } else {
                        mDialogValueEditor.setHint("Variable value");
                    }
                    InputFilter[] lenFilter = new InputFilter[1];
                    lenFilter[0] = new InputFilter.LengthFilter(1);
                    mDialogValueEditor.setFilters(lenFilter);
                    return mDialogValueEditor;
                default:
                    mDialogValueEditor = new EditText(NewVarListFrag.this.getActivity());
                    if (hint != null) {
                        mDialogValueEditor.setHint(hint);
                    } else {
                        mDialogValueEditor.setHint("Variable value");
                    }
                    mDialogValueEditor.setInputType(InputType.TYPE_CLASS_NUMBER);
                    return mDialogValueEditor;
            }
        } else {
            switch (mSelectedItemPosition) {
                default:
                    mDialogValueEditor = new EditText(NewVarListFrag.this.getActivity());
                    if (hint != null) {
                        mDialogValueEditor.setHint(hint);
                    } else {
                        mDialogValueEditor.setHint("Variable value");
                    }
                    return mDialogValueEditor;
            }
        }
    }

    @Nullable
    private Object getGenericValue(@NotNull String value) {
        if (mIsPrimitive) {
            switch (mSelectedItemPosition) {
                case 0:
                    try {
                        return Byte.valueOf(value);
                    } catch (NumberFormatException e) {
                        return errorMessage();
                    }
                case 1:
                    try {
                        return Short.valueOf(value);
                    } catch (NumberFormatException e) {
                        return errorMessage();
                    }
                case 2:
                    try {
                        return Integer.valueOf(value);
                    } catch (NumberFormatException e) {
                        return errorMessage();
                    }
                case 3:
                    try {
                        return Long.valueOf(value);
                    } catch (NumberFormatException e) {
                        return errorMessage();
                    }
                case 4:
                    try {
                        return Float.valueOf(value);
                    } catch (NumberFormatException e) {
                        return errorMessage();
                    }
                case 5:
                    try {
                        return Double.valueOf(value);
                    } catch (NumberFormatException e) {
                        return errorMessage();
                    }
                case 6:
                    return Boolean.valueOf(value);
                case 7:
                    return Character.valueOf(value.charAt(0));
            }
        } else {
            switch (mSelectedItemPosition) {
                case 0: // String
                    return value;
            }
        }

        return null;
    }

    @Nullable private Class<?> getPrimitiveType(int pos) {
        if (mIsPrimitive) {
            switch (pos) {
                case 0:
                    return Byte.class;
                case 1:
                    return Short.class;
                case 2:
                    return Integer.class;
                case 3:
                    return Long.class;
                case 4:
                    return Float.class;
                case 5:
                    return Double.class;
                case 6:
                    return Boolean.class;
                case 7:
                    return Character.class;
            }
        } else {
            switch (pos) {
                case 0:
                    return String.class;
            }
        }
        return null;
    }

    private final DialogInterface.OnClickListener mDialogOkL = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mArrayPositionCount == 0) {
                mReflectionObject = Array.newInstance(getPrimitiveType(mSelectedItemPosition), mArraySizePicker.getValue() + 1);
            } else if (mSelectedItemPosition != 6) {
                Array.set(mReflectionObject, mArrayPositionCount - 1, getGenericValue(mDialogValueEditor.getText().toString()));
            } else if (mDialogTFGroup.getCheckedRadioButtonId() == R.id.radio_true) {
                Array.set(mReflectionObject, mArrayPositionCount - 1, Boolean.TRUE);
            } else {
                Array.set(mReflectionObject, mArrayPositionCount - 1, Boolean.FALSE);
            }

            if (mArrayPositionCount == (mArraySizePicker.getValue()) + 1) {
                addToGlobals();
                StatMethods.hotBread(NewVarListFrag.this.getActivity(), "Finished instantiating", Toast.LENGTH_SHORT);

                NewVarListFrag.this.getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out,
                                R.anim.push_right_in, R.anim.push_right_out)
                        .replace(FragmentUtil.getContentViewCompat(), new VarListFrag(), VarListFrag.FRAG_TAG)
                        .addToBackStack(VarListFrag.FRAG_TAG).commit();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(NewVarListFrag.this.getActivity());

            /* mDialogValueEditor = new EditText(NewVarListFrag.this.getActivity());
            mDialogValueEditor.setHint("Value " + ++mArrayPositionCount + " / " + getArraySize());
            mDialogValueEditor.setInputType(InputType.TYPE_CLASS_NUMBER); */
            builder.setTitle("Enter a value").setView(getDialogView("Value " + ++mArrayPositionCount + " / " + getArraySize()))
                    .setPositiveButton("OK", mDialogOkL)
                    .setNegativeButton("Cancel", mDialogCancelL)
                    .setOnCancelListener(mDialogOnCancelL)
                    .show();
        }
    };

    private final DialogInterface.OnClickListener mDialogCancelL = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };

    private final DialogInterface.OnCancelListener mDialogOnCancelL = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            mArraySizePicker = null;
            mDialogValueEditor = null;
            mArrayPositionCount = 0;
        }
    };

    private final AdapterView.OnItemClickListener mListItemClickL = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSelectedItemPosition = position;
            try {
                if (mIsPrimitive) {
                    switch (position) {
                        case 0:
                            mReflectionCtor = Byte.class.getConstructor(byte.class);
                            break;
                        case 1:
                            mReflectionCtor = Short.class.getConstructor(short.class);
                            break;
                        case 2:
                            mReflectionCtor = Integer.class.getConstructor(int.class);
                            break;
                        case 3:
                            mReflectionCtor = Long.class.getConstructor(long.class);
                            break;
                        case 4:
                            mReflectionCtor = Float.class.getConstructor(float.class);
                            break;
                        case 5:
                            mReflectionCtor = Double.class.getConstructor(double.class);
                            break;
                        case 6:
                            mReflectionCtor = Boolean.class.getConstructor(boolean.class);
                            break;
                        case 7:
                            mReflectionCtor = Character.class.getConstructor(char.class);
                            break;
                    }
                } else {
                    switch (position) {
                        case 0:
                            mReflectionCtor = String.class.getConstructor(String.class);
                            break;
                    }
                }
            } catch (NoSuchMethodException e) {
                StatMethods.hotBread(NewVarListFrag.this.getActivity(), "Error finding class constructor!", Toast.LENGTH_SHORT);
                mReflectionCtor = null;
            }
        }
    };
}
