package com.github.n1try.quiznerd.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
public class QuizUser implements Parcelable {
    private String id;
    private String displayName;
    private String email;
    private String authentication;
    private GenderType gender = GenderType.MALE;

    protected QuizUser(Parcel in) {
        id = in.readString();
        displayName = in.readString();
        email = in.readString();
        authentication = in.readString();
    }

    public static final Creator<QuizUser> CREATOR = new Creator<QuizUser>() {
        @Override
        public QuizUser createFromParcel(Parcel in) {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(in.readString(), QuizUser.class);
        }

        @Override
        public QuizUser[] newArray(int size) {
            return new QuizUser[size];
        }
    };

    @Override
    public int describeContents() {
        return id.hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Gson gson = new GsonBuilder().create();
        parcel.writeString(gson.toJson(this));
    }
}
