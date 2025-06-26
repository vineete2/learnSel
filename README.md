**Selenium Learnings
**

**Update the Configuration File**
The application requires a configuration file to store sensitive information such as the MongoDB URI.
mongodb_uri=
email=
password=
firefox_profile_path=

MongoDB connection URI sample: mongodb+srv://username:password@cluster0.mongodb.net/mydatabase

**File Format and Location**
The config file should be placed at:
C:/config/config.properties

**Usage and Security**
The application reads the URI from this file at startup.
Ensure the file is readable by the application and not accessible to unauthorized users.
**Do not commit this file to version control.**
Store credentials securely and restrict file permissions as needed.
