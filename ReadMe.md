# MetaMap Med Term Frequency Builder

## MetaMap Installation

https://metamap.nlm.nih.gov/download/public_mm_linux_main_2020.tar.bz2

https://metamap.nlm.nih.gov/download/public_mm_linux_javaapi_2020.tar.bz2

copy all files from `javaapi/public_mm/bin` to `public_mm_linux_main_2020/public_mm/bin` 

copy `javaapi/public_mm/bin/mmserver` to `public_mm_linux_main_2020/public_mm`

## MetaMap Server Run

copy files from this repo `./scripts` folder to `public_mm_linux_main_2020/public_mm`

`chmod +x start.sh stop.sh`

to run all servers execute `sh ./start.sh` (mmserver std output will start in terminal)

to stop all servers execute `sh ./stop.sh`

## Example Execution

Simply uncomment line in `MedFrequencyApplication.kt`

```kotlin
@SpringBootApplication
class MedFrequencyApplication : CommandLineRunner {
    override fun run(vararg args: String) {
        runExampleText() //this line should be uncommented
    }
}
```
