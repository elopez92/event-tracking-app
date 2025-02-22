# Event Tracking App

An Android app that allows users to create, manage, and receive reminders for events. Built with efficiency and usability in mind, this app leverages **Room Database**, **ViewModel**, and **LiveData** to ensure smooth performance and data persistence.

##  Features

- **Create Events** – Add events with a name, date, and time.  
- **Edit Events** – Modify existing event details.  
- **Delete Events** – Swipe to delete with a confirmation dialog.  
- **Sort & Filter** – Sort events by date or name and search for specific events.  
- **Event Reminders** – Get notifications for upcoming events.  
- **Offline Support** – Works without an internet connection using Room Database.  

##  Tech Stack

- **Language:** Java  
- **Architecture:** MVVM (Model-View-ViewModel)  
- **Database:** Room Persistence Library  
- **UI Components:** RecyclerView, Swipe Gestures, AlertDialogs  
- **Asynchronous Processing:** LiveData, ViewModel, Executors  
- **Notifications:** AlarmManager, BroadcastReceiver  
