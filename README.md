# Lettore musicale in Java su Android Studio


Al momento è ancora un progetto in sviluppo e contiene solo tre funzioni basilari: Play/Pause, avanti e indietro. Una volta installato, l'app scannerizzerà tutti i file mp3 sullo smartphone Android e sarà possibile riprodurli. Se il dispositivo Android ha una versione uguale o superiore al 6.0 (SDK 23), all'avvio l'app chiederà i permessi di lettura della memoria interna necessari a scannerizzare i file mp3. In questo caso è presente un bug ed è necessario riavviare l'app dopo aver consesso i permessi.

Per il caricamento delle foto album, utilizzare Picasso: Aggiungere in build.gradle (Module:app): compile 'com.squareup.picasso:picasso:2.5.2'

http://square.github.io/picasso/

Aggiornato al 12-10-2017
