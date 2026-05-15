import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    var initialRoute: String?

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(initialRoute: initialRoute)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var initialRoute: String?

    var body: some View {
        ComposeView(initialRoute: initialRoute)
            .ignoresSafeArea()
            .id(initialRoute) // Force recreation if route changes
    }
}
