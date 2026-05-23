# ReporteCiudadano

This is an app that lets you register "potholes" on the streets, avenues, etc.

# Happy path

1. Open the app.
2. The way it's firstly organized is by tabs: "Report", "My Reports" (not editable),"Reports Map".
3. Select button "Register pothole".
4. This button opens a Camera and lets you take up to 4 pictures.
5. The image taken by the camera must have EXIF information, meaning that location service must be
   requested as well.
6. If no location is allowed, then pictures can't be taken, because later the EXIF information will
   be used.
7. Each taken image, there must be options in the preview to continue taking images, retake image or
   complete.
8. When complete, then all image previews will appear in a list, letting you retake image or delete
   any.
9. There must be buttons to cancel everything anytime or to continue to the next step.
10. In the preview images screen, there must be options for including Title and Description of the
    pothole and specs, with respective placeholders (mandatory information).
11. Also, in another non-editable Text there must be the approximated address (extracted from the exif information of one image, then processed from the
    standard maps provider, depending on OS).
12. When entering this last Screen, there must be a popup indicating that it's ok if the location is
    approximated.
13. After the user describes and reviews the information, then a button will appear to send the
    Citizen Report.
14. When sent, a screen with a thankful text for being part of the improvement of the city will
    appear, saying that all this information is valuable and considered by the Public officials.
15. After 4 seconds this latest screen will dismiss and get back to the main one where "Register
    pothole" appears.
16. The Tab "My reports" contains a list with all reports with: Title and Status (the
    status will be implemented later, but initially it will be Sent) which can be Sent, Seen,
    Pending, In Progress, Resolved, Discarded.
17. If the user press on any item of the list, then it will navigate to another screen, similar to "Report" one, but non-editable.
18. The tab with "Reports Map" must show all stored locations of the reports, marked with a pin.
19. When clicking on any pin, then it will navigate to another screen, similar to "Report" one, but non-editable.