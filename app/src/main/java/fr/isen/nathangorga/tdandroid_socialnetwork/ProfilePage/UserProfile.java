package fr.isen.nathangorga.tdandroid_socialnetwork.ProfilePage;


public class UserProfile {
    private String username;
    private String firstName;
    private String lastName;
    private String profilePictureName;//nom de l'image dans la BDD

    public UserProfile(String username, String firstName, String lastName, String profilePictureName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureName = profilePictureName;
    }

    public static UserProfile getFakeUser() {
        return new UserProfile("papy123", "Jean", "Dupont", "pfp_jean.png");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePictureName() {
        return profilePictureName;
    }

    public void setProfilePictureName(String profilePicture) {
        this.profilePictureName = profilePicture;
    }
}
