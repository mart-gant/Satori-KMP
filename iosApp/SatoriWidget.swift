import WidgetKit
import SwiftUI

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date())
    }

    func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
        let entry = SimpleEntry(date: Date())
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        let entries = [SimpleEntry(date: Date())]
        let timeline = Timeline(entries: entries, policy: .atEnd)
        completion(timeline)
    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
}

struct SatoriWidgetEntryView : View {
    var entry: Provider.Entry
    @Environment(\.widgetFamily) var family
    @Environment(\.accessibilityHighContrast) var highContrastEnabled

    var body: some View {
        let mainColor: Color = highContrastEnabled ? .black : .red
        let textColor: Color = highContrastEnabled ? .white : .primary

        switch family {
        case .accessoryCircular:
            ZStack {
                if !highContrastEnabled {
                    AccessoryWidgetBackground()
                } else {
                    Circle().fill(.black)
                }
                Image(systemName: "bolt.heart.fill")
                    .font(.title)
                    .foregroundColor(highContrastEnabled ? .white : .primary)
            }
            .widgetURL(URL(string: "satori://sos"))
        case .accessoryRectangular:
            HStack {
                Image(systemName: "bolt.heart.fill")
                    .foregroundColor(mainColor)
                VStack(alignment: .leading) {
                    Text("Satori SOS")
                        .font(.headline)
                        .fontWeight(highContrastEnabled ? .heavy : .bold)
                    Text("Szybka pomoc")
                        .font(.caption)
                        .foregroundColor(highContrastEnabled ? .white : .secondary)
                }
            }
            .widgetURL(URL(string: "satori://sos"))
        case .accessoryInline:
            Label("Satori SOS", systemImage: "bolt.heart.fill")
                .widgetURL(URL(string: "satori://sos"))
        default:
            VStack {
                Text("Satori SOS")
                    .font(.headline)
                    .foregroundColor(textColor)
                Button(action: {}) {
                    Label("Pomoc", systemImage: "bolt.heart.fill")
                        .fontWeight(highContrastEnabled ? .black : .medium)
                }
                .buttonStyle(.borderedProminent)
                .tint(mainColor)
                .foregroundColor(highContrastEnabled ? .white : .primary)
            }
            .widgetURL(URL(string: "satori://sos"))
            .containerBackground(highContrastEnabled ? Color.black : Color.clear, for: .widget)
        }
    }
}

struct SatoriWidget: Widget {
    let kind: String = "SatoriWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            if #available(iOS 17.0, *) {
                SatoriWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                SatoriWidgetEntryView(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName("Satori SOS")
        .description("Szybki dostęp do porad w sytuacjach przebodźcowania.")
        .supportedFamilies([.accessoryCircular, .accessoryRectangular, .accessoryInline, .systemSmall])
    }
}
