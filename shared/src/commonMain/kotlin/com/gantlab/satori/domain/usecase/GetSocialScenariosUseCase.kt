package com.gantlab.satori.domain.usecase

import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.db.SocialScenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetSocialScenariosUseCase(private val repository: SatoriRepository) {
    suspend operator fun invoke(): List<SocialScenario> = withContext(Dispatchers.Default) {
        val scenarios = repository.getAllScenarios()
        if (scenarios.isEmpty()) {
            seedInitialScenarios()
            repository.getAllScenarios()
        } else {
            scenarios
        }
    }

    private fun seedInitialScenarios() {
        repository.insertScenario(
            "Wizyta u lekarza", 
            "Co zrobić po wejściu do przychodni.",
            "1. Podejdź do rejestracji\n2. Podaj swoje imię i nazwisko\n3. Usiądź w poczekalni i czekaj na wywołanie\n4. Wejdź do gabinetu, gdy lekarz Cię poprosi",
            "Zdrowie"
        )
        repository.insertScenario(
            "Zakupy w sklepie", 
            "Jak sprawnie zrobić zakupy.",
            "1. Przygotuj listę produktów\n2. Weź koszyk przy wejściu\n3. Znajdź produkty z listy\n4. Podejdź do kasy i zapłać",
            "Codzienność"
        )
        repository.insertScenario(
            "Rozmowa telefoniczna",
            "Jak przygotować się i przeprowadzić rozmowę.",
            "1. Zapisz na kartce główny cel rozmowy\n2. Wybierz numer i poczekaj na odebranie\n3. Przywitaj się: 'Dzień dobry, mówi [Twoje Imię]'\n4. Powiedz, w jakiej sprawie dzwonisz\n5. Słuchaj odpowiedzi i ewentualnie zapisuj ważne informacje\n6. Na koniec powiedz 'Dziękuję, do widzenia' i rozłącz się",
            "Komunikacja"
        )
        repository.insertScenario(
            "Jazda autobusem",
            "Korzystanie z komunikacji miejskiej krok po kroku.",
            "1. Sprawdź numer autobusu i godzinę na rozkładzie\n2. Stań na przystanku i czekaj na nadjeżdżający pojazd\n3. Upewnij się, że to Twój numer i wejdź do środka\n4. Skasuj bilet lub przyłóż kartę do czytnika\n5. Znajdź wolne miejsce siedzące lub złap się stabilnie uchwytu\n6. Obserwuj tablicę z przystankami lub słuchaj komunikatów\n7. Przed Twoim przystankiem naciśnij przycisk 'Stop' i wysiądź",
            "Podróż"
        )
    }
}
