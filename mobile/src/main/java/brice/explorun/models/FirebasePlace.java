package brice.explorun.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Keep;

@Keep
public class FirebasePlace implements Parcelable
{
	private String name;
	private Position position;

	public String getName()
	{
		return this.name;
	}

	public Position getPosition()
	{
		return this.position;
	}

	// Constructor required by Firestore
	public FirebasePlace(){}

	public FirebasePlace(String name, Position position)
	{
		this.name = name;
		this.position = position;
	}

	protected FirebasePlace(Parcel in)
	{
		name = in.readString();
		position = (Position) in.readValue(Position.class.getClassLoader());
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(name);
		dest.writeValue(position);
	}

	@SuppressWarnings("unused")
	public static final Parcelable.Creator<FirebasePlace> CREATOR = new Parcelable.Creator<FirebasePlace>()
	{
		@Override
		public FirebasePlace createFromParcel(Parcel in) {
			return new FirebasePlace(in);
		}

		@Override
		public FirebasePlace[] newArray(int size) {
			return new FirebasePlace[size];
		}
	};
}
