package com.example.notes.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.database.NoteDataBase;
import com.example.notes.databinding.ActivityCreateNoteBinding;
import com.example.notes.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CreateNoteActivity extends AppCompatActivity {

    ActivityCreateNoteBinding binding;
    private int selectedNoteColor;
    private String selectedImagePath;
    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;
    private Note alreadyAvailableNote;

    public static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    public static final int REQUEST_CODE_SELECT_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imgBack.setOnClickListener(view -> onBackPressed());

        binding.txtDateTime.setText(
                new SimpleDateFormat("EEEE,dd MMMM yyyy HH:mm:a", Locale.getDefault())
                        .format(new Date())
        );
        binding.imgSave.setOnClickListener(view -> saveNote());
        Log.d("database", String.valueOf(getApplicationContext().getDatabasePath("notes_db.db")));

        selectedNoteColor = getResources().getColor(R.color.colorDefaultNote);
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            isViewOrUpdateNote();
        }

        binding.imgRemoveDeleteURL.setOnClickListener(view -> {
            binding.txtWebURL.setText(null);
            binding.layoutWebURL.setVisibility(View.GONE);
        });

        binding.imgRemoveImage.setOnClickListener(view -> {
            binding.imgNote.setImageBitmap(null);
            binding.imgNote.setVisibility(View.GONE);
            binding.imgRemoveImage.setVisibility(View.GONE);
            selectedImagePath = "";
        });

        if (getIntent().getBooleanExtra("isFromQuickActions", false)) {
            String type = getIntent().getStringExtra("quickActionType");
            if (type != null) {
                if (type.equals("image")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    binding.imgNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    binding.imgNote.setVisibility(View.VISIBLE);
                    binding.imgRemoveImage.setVisibility(View.VISIBLE);
                } else if (type.equals("URL")) {
                    binding.txtWebURL.setText(getIntent().getStringExtra("URL"));
                    binding.layoutWebURL.setVisibility(View.VISIBLE);
                    binding.imgRemoveDeleteURL.setVisibility(View.VISIBLE);
                }
            }
        }

        initMiscellaneous();

    }

    private void isViewOrUpdateNote() {
        binding.inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        binding.inputNote.setText(alreadyAvailableNote.getNoteText());
        binding.txtDateTime.setText(alreadyAvailableNote.getDateTime());

        if (alreadyAvailableNote.getImgPath() != null && !alreadyAvailableNote.getImgPath().trim().isEmpty()) {
            binding.imgNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImgPath()));
            binding.imgNote.setVisibility(View.VISIBLE);
            binding.imgRemoveImage.setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImgPath();
        }
        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()) {
            binding.txtWebURL.setText(alreadyAvailableNote.getWebLink());
            binding.layoutWebURL.setVisibility(View.VISIBLE);
        }

    }


    private void saveNote() {
        if (binding.inputNoteTitle.getText().toString().trim().isEmpty()) {
            showToast("note title cant be empty");
            return;
        } else if (binding.inputNote.getText().toString().trim().isEmpty() && binding.imgNote.getDrawable().toString().isEmpty()
                && binding.txtWebURL.getText().toString().trim().isEmpty()) {
            showToast("note can not be empty");
            return;
        }

        final Note note = new Note();
        note.setTitle(binding.inputNoteTitle.getText().toString().trim());
        note.setNoteText(binding.inputNote.getText().toString().trim());
        note.setDateTime(binding.txtDateTime.getText().toString().trim());
        note.setColor(selectedNoteColor);
        note.setImgPath(selectedImagePath);

        if (binding.layoutWebURL.getVisibility() == View.VISIBLE) {
            note.setWebLink(binding.txtWebURL.getText().toString());
        }

        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }

        class saveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDataBase.getDataBase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new saveNoteTask().execute();
    }

    @SuppressLint("NonConstantResourceId")
    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layoutMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.txtMiscellaneous).setOnClickListener(view -> {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        final ImageView imgColor1 = layoutMiscellaneous.findViewById(R.id.imgColor1);
        final ImageView imgColor2 = layoutMiscellaneous.findViewById(R.id.imgColor2);
        final ImageView imgColor3 = layoutMiscellaneous.findViewById(R.id.imgColor3);
        final ImageView imgColor4 = layoutMiscellaneous.findViewById(R.id.imgColor4);
        final ImageView imgColor5 = layoutMiscellaneous.findViewById(R.id.imgColor5);

        layoutMiscellaneous.findViewById(R.id.viewColor1).setOnClickListener(view -> {
            selectedNoteColor = getResources().getColor(R.color.colorDefaultNote);
            imgColor1.setImageResource(R.drawable.ic_done);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);

        });
        layoutMiscellaneous.findViewById(R.id.viewColor2).setOnClickListener(view -> {
            selectedNoteColor = getResources().getColor(R.color.colorNote2);
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(R.drawable.ic_done);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);

        });
        layoutMiscellaneous.findViewById(R.id.viewColor3).setOnClickListener(view -> {
            selectedNoteColor = getResources().getColor(R.color.colorNote3);
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(R.drawable.ic_done);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);

        });
        layoutMiscellaneous.findViewById(R.id.viewColor4).setOnClickListener(view -> {
            selectedNoteColor = getResources().getColor(R.color.colorNote4);
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(R.drawable.ic_done);
            imgColor5.setImageResource(0);

        });
        layoutMiscellaneous.findViewById(R.id.viewColor5).setOnClickListener(view -> {
            selectedNoteColor = getResources().getColor(R.color.colorNote5);
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(R.drawable.ic_done);

        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() > 0 && !String.valueOf(alreadyAvailableNote.getColor()).isEmpty()) {
//TODO fix the color
            switch (alreadyAvailableNote.getColor()) {
                case R.color.colorNote2:
                    layoutMiscellaneous.findViewById(R.id.viewColor2).performClick();
                    break;
                case R.color.colorNote3:
                    layoutMiscellaneous.findViewById(R.id.viewColor3).performClick();
                    break;
                case R.color.colorNote4:
                    layoutMiscellaneous.findViewById(R.id.viewColor4).performClick();
                    break;
                case R.color.colorNote5:
                    layoutMiscellaneous.findViewById(R.id.viewColor5).performClick();
                    break;

            }

        }


        layoutMiscellaneous.findViewById(R.id.layoutAddImage).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            if (ContextCompat.checkSelfPermission(
                    getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        CreateNoteActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE_PERMISSION
                );
            } else {
                selectImage();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layoutAddUrl).setOnClickListener(view -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showDialogAddURL();
        });

        if (alreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNoteContainer).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layoutDeleteNoteContainer).setOnClickListener(view -> {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDialogDeleteNote();
            });
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                showToast("permission denied !");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectImageUri = data.getData();
                if (selectImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imgNote.setImageBitmap(bitmap);
                        binding.imgNote.setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectImageUri);
                        binding.imgRemoveImage.setVisibility(View.VISIBLE);


                    } catch (Exception e) {
                        showToast(e.getMessage());
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showDialogAddURL() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url, findViewById(R.id.layoutAddUrlContainer), false
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputURL = view.findViewById(R.id.inputUrl);
            //inputURL.requestFocus();
            view.findViewById(R.id.txtAdd).setOnClickListener(view1 -> {
                if (inputURL.getText().toString().isEmpty()) {
                    showToast("Enter URL");
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                    showToast("Enter valid URL");
                } else {
                    binding.txtWebURL.setText(inputURL.getText().toString());
                    binding.layoutWebURL.setVisibility(View.VISIBLE);
                    dialogAddURL.dismiss();
                }
            });
            view.findViewById(R.id.txtCancel).setOnClickListener(view12 -> dialogAddURL.dismiss());
        }
        dialogAddURL.show();
    }

    private void showDialogDeleteNote() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note, findViewById(R.id.layoutDeleteNoteContainer), false
            );
            builder.setView(view);

            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {

                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.txtDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDataBase.getDataBase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();

                }
            });
            view.findViewById(R.id.txtCancel).setOnClickListener(view1 -> dialogDeleteNote.dismiss());
        }
        dialogDeleteNote.show();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}