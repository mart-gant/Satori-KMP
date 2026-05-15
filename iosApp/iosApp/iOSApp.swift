import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @State private var initialRoute: String? = nil

    var body: some Scene {
        WindowGroup {
            ContentView(initialRoute: initialRoute)
                .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("NavigateToSOS"))) { _ in
                    self.initialRoute = "tips"
                }
                .onOpenURL { url in
                    if url.scheme == "satori" && url.host == "sos" {
                        self.initialRoute = "tips"
                    }
                }
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        if let shortcutItem = options.shortcutItem {
            handleShortcutItem(shortcutItem)
        }
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func windowScene(_ windowScene: UIWindowScene, performActionFor shortcutItem: UIApplicationShortcutItem, completionHandler: @escaping (Bool) -> Void) {
        handleShortcutItem(shortcutItem)
        completionHandler(true)
    }

    private func handleShortcutItem(_ shortcutItem: UIApplicationShortcutItem) {
        if shortcutItem.type == "com.gantlab.satori.sos" {
            NotificationCenter.default.post(name: NSNotification.Name("NavigateToSOS"), object: nil)
        }
    }
}
