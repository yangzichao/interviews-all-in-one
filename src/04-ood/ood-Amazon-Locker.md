
class Location // aka LockerManager
// field
- 
// method
+ acceptBox
+ releaseBox
- findNextFitLocker
- canBoxFitInLocker

class Locker
// field
- LockerType
- id

enum LockerType
- dimension


class Box (note that box can be any dimension because it is not necessarily an amazon standard box);
- dimension

if you want to have box have some sort of enum type you can add a new constructor.
