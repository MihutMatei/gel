# CivicLens - Hackathon MVP

## Project Architecture
- **Clean Architecture**: Divided into `data`, `domain`, and `presentation` layers.
- **Domain**: Contains business logic (use cases) and models. Repositories are interfaces here.
- **Data**: Implementation of repositories (currently fake for MVP speed).
- **Presentation**: Compose Multiplatform screens and ViewModels.
- **Map Abstraction**: `MapFacade` allows swapping the placeholder map with a real SDK (e.g. MapLibre) later.

## How to Run
- **Android**: Run the `:composeApp` module on an Android emulator or device.
- **Desktop**: Run the `:composeApp` module using the Gradle task `:composeApp:run`.

## Future Improvements
- **Real Backend**: Replace `FakeIssueRepository` and others with Ktor client implementations calling a real REST API.
- **Map SDK**: Replace `FakeMapFacade` with an implementation using MapLibre or Google Maps.
- **iOS Support**: Add a `composeApp/src/iosMain` target and set up the Xcode project.

## Fake API Contract
The following endpoints are simulated in the repository layer:
- `GET /issues` -> `IssueRepository.getIssues()`
- `GET /issues/{id}` -> `IssueRepository.getIssueById(id)`
- `POST /issues` -> `IssueRepository.createIssue(issue)`
- `POST /comments` -> `CommentRepository.addComment(comment)`
- `PATCH /issues/{id}/status` -> `IssueRepository.updateIssueStatus(id, status)`
