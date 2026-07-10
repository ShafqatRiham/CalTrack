CalTrack 

Features
- Tracking food eaten and calories taken per day
- Leaderboard system to compare with other users
- Streaks to encourage users to record and maintain their calories
- Logging meals with the user-selected time
- Logging the number of steps for calorie reduction
- Setting up a calorie goal for the day

Frontend: Kotlin, Android Studio, Composed Functions
Backend: Node.js, Express, MySQL

Necessary Programs :
1. Browser
2. Node.js
3. MySQL
4. VS Code
5. Android Studio
6. Git
7. Npm install
8. Java SDK (latest is 26)

Installations:
1. Download git from https://git-scm.com/install/
2. Download Node.js from https://nodejs.org/en/download
3. Download Caltrack and Caltrack_API project through zip from Github 
4. MySQL - “https://dev.mysql.com/downloads/workbench/”
5. VS Code - https://code.visualstudio.com/download
6. Download and install JDK: https://www.oracle.com/jp/java/technologies/downloads/
7. Download and install Android Studio: https://developer.android.com/studio

Steps for installing MySQL

1. Open your browser
2. Go to MySQL or the link “https://dev.mysql.com/downloads/workbench/”
3. Download and install MySQL Workbench for your OS and system
4. Open MySQL Workbench
5. Create a connection with a host name and password
6. Go to the SQL tab and enter (CREATE DATABASE ‘database name’)
7. Once you're in your server, go to server -> data import
8. Select the import self-contained file and browse to the database file inside the CalTrack_API folder to choose it
9. On the default target schema, click on new and call it caltrack_project_db
10. Click "start import"
11. Wait until the import is completed

Running the API in VS Code
1. Either in VS Code or File Explorer, create the .env file in the root of Caltrack_API folder
2. Open the .env file and fill it up with this as a basis

DB_HOST=localhost

DB_USER=root

DB_PASSWORD=your_actual_mysql_password

DB_NAME=caltrack_project_db

JWT_SECRET=makethisalongrandomunguessablestring

PORT=3000

4. Replace your_actual_mysql_password with the password you set when creating your MySQL connection. If you did not set a password, leave it empty (DB_PASSWORD=)
5. Open the command terminal in VS Code
6. In it, install the dependencies by typing "npm install" but make sure your terminal is inside the CalTrack_API folder before running. (You can go to the project folder in File Explorer, copy the path, and paste "cd {path}" in the command terminal)
7. You can start the server by typing "node src/app.js"

Running the mobile app in Android Studio
1. Go to Android Studio
2. Click on open project and navigate to the "Caltrack" folder
3. After getting in, click on sync now
4. To get the emulator running, go to "Tools → Device Manager"
   - Click on "Create Device"
   - Select "Pixel 8" from the device list and then click "Next"
   -  Select a system image of "API level 34 or higher"
   - If you don't see one, click **Download** next to it and wait for it to download
   - Click "Next" then "Finish"
5. Run the app but make sure the server in VS code is running\
6. You will be sent to the log in screen to then be able to register or sign in
