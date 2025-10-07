E-Index (Java) — Distribuirana klijent/server konzolna aplikacija

Pokretanje
java -cp .\out eindex.server.Server 5555 (terminal 1)
java -cp .\out eindex.client.Client 127.0.0.1 5555 (terminal 2)

JMBG universal: 0101000700011


- Server: pokrenuti `eindex.server.Server` (opciono proslediti port, podrazumevano `5555`).
- Klijent: pokrenuti `eindex.client.Client` (opciono `host` i `port`).

Funkcionalnosti

- Logovanje na osnovu `users.txt` (`username:password:role`). Na prvom pokretanju kreira se `admin:admin:admin` ako ne postoji.
- Administrator: dodavanje admina, dodavanje studenata (validacija indeksa i JMBG), dodavanje predmeta sa kategorijama (zbir max=100, min<=max), dodeljivanje predmeta studentu, ažuriranje poena.
- Student: pregled poena po kategorijama, pregled položenih/nepoloženih i ocena.
- Čuvanje stanja (`state.ser`) pri gašenju servera i na zahtev.

Napomene

- Validacija broja indeksa: `^[Ee][123][/-](200\d|201\d|202[0-3])$`.
- JMBG: 13 cifara, validan dan i mesec (osnovna provera).
- Ocene: 0–50=5, 51–60=6, 61–70=7, 71–80=8, 81–90=9, 91–100=10, uz uslov min poena po kategorijama i minimum 51.

