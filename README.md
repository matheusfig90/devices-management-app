# Devices Management App

This is a simple device management app, which allows users to book and return devices.
Additionally, for each device, it returns its availability, when it was booked and who booked it.

### Requirements
- JDK 17
- PostgreSQL (only if you don't want to use docker)
- Docker

### Running

To run the database, you can run:
```
# If you don't want to run as daemon (background), just ignore the '-d' argument 
docker-compose up -d
```

#### Running manually
To run manually, you need to type:
```
# Building the jar...
./gradlew build

# ... and running it
java -jar build/libs/devices-management-app-1.0-SNAPSHOT.jar
```

#### Running with IntelliJ
If you are using IntelliJ or any IDE, just click to run the `Application.java` under `src/com/matheusfig90`.

#### Endpoints
After running the application, the application will be available at `http://localhost:8080`.
```
# Get device info
curl http://localhost:8080/devices/1

# Book a device
curl -X PUT -H "Content-Type: application/json" --data '{ "userId": 1 }' http://localhost:8080/devices/1/book

# Return a device
curl -X PUT -H "Content-Type: application/json" http://localhost:8080/devices/1/return
```

The list of initial devices are available at `src/main/resources/db/migration/V1__devices.sql`.

### Testing
To execute the unit tests, you can run:
```
./gradlew test
```

### Next steps
- [ ] Add user authentication, to avoid receive `userId` as param
- [ ] Create a DTO between controller and service, to avoid exposing entities
- [ ] Set up CI/CD