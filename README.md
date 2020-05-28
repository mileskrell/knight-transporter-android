# Knight Transporter (Android)

This is a Rutgers bus-tracking app.

It aims to combine features that currently exist in two separate applications:
- [TransLoc Rider](https://translocrider.com)*
  - displaying current bus locations on a map
  - displaying bus routes on a map
  - displaying arrival estimates
- <https://maps.rutgers.edu>*
  - displaying Rutgers-affiliated buildings, parking lots, and walkways on a map
  - allowing the user to search for Rutgers-affiliated buildings and parking lots
  - displaying details for these buildings and parking lots (such as address, photo, list of associated departments, etc.)

This app is under heavy development, with a focus on adding new features, rather than fixing bugs that don't impact regular use.
As a result, there are some bugs that I'm aware of but choosing not to focus on for now (such as a lack of handling of configuration changes and network connectivity loss).

###### *Knight Transporter is being developed for Rutgers in an official capacity, but is unaffiliated with these other projects.

## Building and running

1. [Obtain a Mapbox token](https://account.mapbox.com/access-tokens/create).
2. Add a string named `mapboxToken` containing this token to the package `edu.rutgers.knighttransporter`. I'd suggest placing it in `app/src/main/java/edu/rutgers/knighttransporter/Keys.kt`, since I've listed that file in `.gitignore`.
3. Run the bus server: <https://github.com/RidhwaanDev/rutgersql> (commit `5abde53732d7e1ee39c7bc70b433c37fae5b7cea`).
4. Update the address for the bus server in the app, which is `translocUrl` in `app/src/main/java/edu/rutgers/knighttransporter/feature_stuff/Urls.kt`.
5. I've encountered a graphical issue with Mapbox on emulators (some icons on the map appear solid black), so I suggest running the app on a physical device.
