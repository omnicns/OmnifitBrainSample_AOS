# OmnifitBrain
> 영어로 읽기: [ENGLISH](README.en.md)

## 개요

본 라이브러리는 옴니핏 제품의 브레인 제품군에 해당하는 장치를 사용하여 
서비스를 제공할 수 있는 안드로이드 앱을 만들 수 있도록 지원하기 위한 것입니다.
라이브러리의 제공되는 기능은 아래와 같이 구성됩니다.

* 브레인 장치 검색 / 취소
* 브레인 장치 연결 / 해제
* 브레인 장치 검색 및 연결 / 취소 및 해제
* 측정 시작 / 종료 (뇌파 데이터 획득)
* 장치 정보 획득
* 장치 상태 모니터링 (연결 상태, 전극 센서 부착 상태, 배터리 잔량)

**검색 중인지를 나타내는 상태, 연결 중인지를 나타내는 상태, 측정 중인지를 나타내는 상태, 뇌파 데이터, 장치 상태 모두 
안드로이드의 LiveData 라이브러리를 사용하여 제공하고 있습니다.**

<br/>

## 라이브러리 사용 설명

### 라이브러리 초기화

ViewModel 클래스를 사용해서 브레인 제품과 통신을 수행하기에 우선 ViewModel 클래스의 초기화가 필요합니다.<br/>
SDK에서 제공하고 있는 ViewModel 클래스명은 **OCWH20ViewModel** 입니다.

#### Activity-KTX 기능 사용

```kotlin
class SampleActivity: AppCompatActivity() {
    private val viewModel by viewModels<OCWH20ViewModel>()
}
```

#### ViewModelProvider 사용

```kotlin
class SampleViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return modelClass.getConstructor(Application::class.java).newInstance(app)
        }
    }


class MainActivity : AppCompatActivity() {
    private var viewModel: OCWH20ViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this, 
            SampleViewModelFactory(application))[OCWH20ViewModel::class.java]
    }
}
```

<br/>

### 브레인 장치 검색/취소

ViewModel의 **find 함수**를 호출하면 장치 검색이 시작되고 **isScanning 값**이 true로 변경됩니다. <br/>
isScanning 값이 true일 때 find 함수를 다시 한번 호출하거나 stopFind 함수를 호출하면 isScanning 값이 false로 바뀌며 검색이 취소됩니다.<br/>
검색이 수행되는 도중에는 scannedBluetoothDevices로 브레인 장치들이 갱신되고, 
검색이 완료되면 scannedBluetoothDevice로 가장 큰 Rssi 값을 가진 기기가 갱신됩니다. 

#### find 함수 매개 변수

> duration : 기기 검색 시간<br/>
> onError : 에러가 발생했을 때 호출되는 콜백. 인자로 Throwable이 들어옴

#### 사용 예시

```kotlin
// 장치 검색
viewModel.find(duration = 10, onError = { throwable ->
    runOnUiThread {
        Toast.makeText(applicationContext, throwable.message, Toast.LENGTH_SHORT).show()
    }
})

// 장치 검색 취소
viewModel.Finding()

// 검색 상태
viewModel.isScanning.observe(this@SampleActivity) { value ->
    if (value) {
        // true -> 검색 중
    } else {
        // false -> 대기 중
    }
}
```

#### 결과

```kotlin
viewModel.scannedBluetoothDevices.observe(this@SampleActivity) { devices ->
    // 주변의 브레인 장치들
}

viewModel.scannedBluetoothDevice.observe(this@SampleActivity) { device ->
    // 최대 Rssi 값을 가진 브레인 장치(가장 거리가 가까운 장치)
}
```

<br/>

### 브레인 장치 연결/해제

ViewModel의 **connect 함수**를 호출하면 연결이 시작되고 **isConnecting 값**이 true로 변경됩니다.<br/>
isConnecting 값이 true일 때 connect 함수를 다시 한번 호출하거나 **disconnect 함수**를 호출하면 isConnecting 값이 false로 바뀌며 연결이 끊깁니다.<br/>
참고로 연결이 끊기면 `Disconnect from device` 라는 문구를 콜백 메서드로 받을 수 있습니다.

#### connect 함수 매개 변수

> device : 연결하기를 원하는 기기. 디폴트 파라미터로 scannedBluetoothDevice가 선언되어있음<br/>
> isAutoConnect : auto connect 옵션을 설정하는 Boolean 값<br/> 
> onError : 에러가 발생했을 때 호출되는 콜백. 인자로 Throwable이 들어옴

#### 사용 예시

```kotlin
// 장치 연결
viewModel.connect(device = viewModel.scannedBluetoothDevice.value, isAutoConnect = false, onError = { throwable ->
    runOnUiThread {
        Toast.makeText(applicationContext, throwable.message, Toast.LENGTH_SHORT).show()
    }
})

// 연결 해제
viewModel.disconnect()

// 연결 상태
viewModel.isConnecting.observe(this@SampleActivity) { value ->
    if (value) {
        // true -> 연결 중
    } else {
        // false -> 대기 중(연결 안됨)
    }
}
```

<br/>

### 브레인 장치 검색 및 연결

ViewModel의 **findWithConnect 함수**를 호출하면 장치 검색 및 연결을 연속적으로 수행합니다.<br/>
검색 단계를 수행중일 때 함수를 다시 한번 호출하면 검색이 중단되고, 연결을 수행중일 때 함수를 다시 한번 호출하면 연결이 끊깁니다.<br/>
**isScanningOrConnecting** 값을 확인하면 해당 함수가 수행 중인지 파악할 수 있습니다.

#### findWithConnect 함수 매개 변수

> duration : 기기 검색 시간<br/>
> device : 이미 발견된 기기를 인자로 넘긴다면, 검색을 수행하지 않고 연결만 수행. 디폴트 파라미터로 null이 선언되어있음<br/>
> onError : 에러가 발생했을 때 호출되는 콜백. 인자로 Throwable이 들어옴

#### 사용 예시
```kotlin
// 장치 검색 및 연결
viewModel.findWithConnect(duration = 10, device = null, onError = { throwable ->
    Toast.makeText(applicationContext, throwable.message, Toast.LENGTH_SHORT).show()
})

// 검색 및 연결 상태
viewModel.isScanningOrConnecting.observe(this@SampleActivity) { value ->
    if (value) {
        // true -> 검색 중이거나 연결 중
    } else {
        // false -> 대기 중
    }
}
```

<br/>

### 측정 시작 / 종료

ViewModel의 **startMeasuring 함수**를 호출하면 측정을 시작되고 **isMeasuring 값**이 true로 변경됩니다<br/>
isMeasuring 값이 true 일 때 startMeasuring 함수를 다시 한번 호출하거나 **stopMeasuring 함수**를 호출하면 isMeasuring 값이 false로 바뀌며 측정이 종료됩니다.
또는 인자로 넘긴 측정 시간이 종료되면 측정을 종료합니다.<br/>
측정이 시작되면 2초 단위로 result라는 LiveData<Result>가 갱신됩니다.<br/>
Result 데이터 클래스 프로퍼티로 설정된 뇌파 측정 데이터에 대한 자세한 내용은 다음과 같습니다.

### Result 데이터 클래스 데이터 획득

|**프로퍼티**|**데이터구분**|**의미**|**범위**|
|:---:|:---:|:---:|:---:|
|isLossOccur|`패킷 유실 발생체크값`|패킷 유실이 발생했는지 체크하는값(사용하면 안되는 값인지를 나타냄)|true, false|
|leftThetaIndicatorValue|`좌뇌쎄타 크기판정값`|좌뇌 쎄타파(4-8Hz미만) 절대파워의 크기판정값|0~10|
|rightThetaIndicatorValue|`우뇌쎄타 크기판정값`|우뇌 쎄타파(4-8Hz미만) 절대파워의 크기판정값|0~10|
|leftAlphaIndicatorValue|`좌뇌알파 크기판정값`|좌뇌 알파파(8-12Hz미만) 절대파워의 크기판정값|0~10|
|rightAlphaIndicatorValue|`우뇌알파 크기판정값`|우뇌 알파파(8-12Hz미만) 절대파워의 크기판정값|0~10|
|leftLowBetaIndicatorValue|`좌뇌Low베타 크기판정값`|좌뇌 Low베타파(12-15Hz미만) 절대파워의 크기판정값|0~10|
|rightLowBetaIndicatorValue|`우뇌Low베타 크기판정값`|우뇌 Low베타파(12-15Hz미만) 절대파워의 크기판정값|0~10|
|leftMiddleBetaIndicatorValue|`좌뇌Middle베타 크기판정값`|좌뇌 Middle베타파(15-20Hz미만) 절대파워의 크기판정값|0~10|
|rightMiddleBetaIndicatorValue|`우뇌Middle베타 크기판정값`|우뇌 Middle베타파(15-20Hz미만) 절대파워의 크기판정값|0~10|
|leftHighBetaIndicatorValue|`좌뇌High베타 크기판정값`|좌뇌 High베타파(20-30Hz미만) 절대파워의 크기판정값|0~10|
|rightHighBetaIndicatorValue|`우뇌High베타 크기판정값`|우뇌 High베타파(20-30Hz미만) 절대파워의 크기판정값|0~10|
|leftGammaIndicatorValue|`좌뇌감마 크기판정값`|좌뇌 감마파(30-40Hz미만) 절대파워의 크기판정값|0~10|
|rightGammaIndicatorValue|`우뇌감마 크기판정값`|우뇌 감마파(30-40Hz미만) 절대파워의 크기판정값|0~10|
|concentrationIndicatorValue|`집중 크기판정값`|집중 크기판정값|0~10|
|leftRelaxationIndicatorValue|`좌뇌이완 크기판정값`|좌뇌이완 크기판정값|0~10|
|rightRelaxationIndicatorValue|`우뇌 이완 크기판정값`|우뇌 이완 크기판정값|0~10|
|unbalanceIndicatorValue|`좌우뇌균형 크기판정값`|좌우뇌균형 크기판정값|0~10|
|leftPowerSpectrum|`자뇌 파워스펙트럼`|좌뇌 파워 스펙트럼(0 ~ 73번 인덱스를 가진 Double Array)|각 인덱스의 값 0~655.35|
|rightPowerSpectrum|`우뇌 파워스펙트럼`|우뇌 파워 스펙트럼(0 ~ 73번 인덱스를 가진 Double Array)|각 인덱스의 값 0~655.35|
|leftThetaPowerSpectrum|`좌뇌세타 파워스펙트럼`|좌뇌세타 파워스펙트럼|0~655.35 * 8|
|rightThetaPowerSpectrum|`우뇌세타 파워스펙트럼`|우뇌세타 파워스펙트럼|0~655.35 * 8|
|leftAlphaPowerSpectrum|`좌뇌알파 파워스펙트럼`|좌뇌알파 파워스펙트럼|0~655.35 * 8|
|rightAlphaPowerSpectrum|`우뇌알파 파워스펙트럼`|우뇌알파 파워스펙트럼|0~655.35 * 8|
|leftLowBetaPowerSpectrum|`좌뇌Low베타 파워스펙트럼`|좌뇌Low베타 파워스펙트럼|0~655.35 * 6|
|rightLowBetaPowerSpectrum|`우뇌Low베타 파워스펙트럼`|우뇌Low베타 파워스펙트럼|0~655.35 * 6|
|leftMidBetaPowerSpectrum|`좌뇌Middle베타 파워스펙트럼`|좌뇌Middle베타 파워스펙트럼|0~655.35 * 10|
|rightMidBetaPowerSpectrum|`우뇌Middle베타 파워스펙트럼`|우뇌Middle베타 파워스펙트럼|0~655.35 * 10|
|leftHighBetaPowerSpectrum|`좌뇌High베타 파워스펙트럼`|좌뇌High베타 파워스펙트럼|0~655.35 * 21|
|rightHighBetaPowerSpectrum|`우뇌High베타 파워스펙트럼`|우뇌High베타 파워스펙트럼|0~655.35 * 21|
|leftGammaPowerSpectrum|`좌뇌감마 파워스펙트럼`|좌뇌감마 파워스펙트럼|0~655.35 * 21|
|rightGammaPowerSpectrum|`우뇌감마 파워스펙트럼`|우뇌감마 파워스펙트럼|0~655.35 * 21|
|leftTotalPowerSpectrum|`좌뇌 파워스펙트럼 합`|좌뇌 파워스펙트럼 합|0~655.35 * 74|
|rightTotalPowerSpectrum|`우뇌 파워스펙트럼 합`|우뇌 파워스펙트럼 합|0~655.35 * 74|
|leftThetaRatio|`좌뇌쎄타 비율`|좌뇌쎄타 비율|0~100|
|leftAlphaRatio|`좌뇌알파 비율`|좌뇌알파 비율|0~100|
|leftLowBetaRatio|`좌뇌Low베타 비율`|좌뇌Low베타 비율|0~100|
|leftMidBetaRatio|`좌뇌Mid베타 비율`|좌뇌Mid베타 비율|0~100|
|leftHighBetaRatio|`좌뇌High베타 비율`|좌뇌High베타 비율|0~100|
|leftGammaRatio|`좌뇌감마 비율`|좌뇌감마 비율|0~100|
|rightThetaRatio|`우뇌쎄타 비율`|우뇌쎄타 비율|0~100|
|rightAlphaRatio|`우뇌알파 비율`|우뇌알파 비율|0~100|
|rightLowBetaRatio|`우뇌Low베타 비율`|우뇌Low베타 비율|0~100|
|rightMidBetaRatio|`우뇌Mid베타 비율`|우뇌Mid베타 비율|0~100|
|rightHighBetaRatio|`우뇌High베타 비율`|우뇌High베타 비율|0~100|
|rightGammaRatio|`우뇌감마 비율`|우뇌감마 비율|0~100|
|leftSEF90|`좌뇌 SEF90`|좌뇌 SEF90|0~36.11|
|rightSEF90|`우뇌 SEF90`|우뇌 SEF90|0~36.11|

<br/>

#### 파워 스펙트럼 구간(1 인덱스당 주파수 증가 단위는 0.488Hz)

|**데이터 구분**|**Hz 범위**|**인덱스 구간**|**수량**|
|:---:|:---:|:---:|:---:|
|THETA|4 ~ 8Hz 미만|left/rightPowerSpectrum[0 ~ 7]|8|
|ALPHA|8 ~ 12Hz 미만|left/rightPowerSpectrum[8 ~ 15]|8|
|L-BETA|12 ~ 15Hz 미만|left/rightPowerSpectrum[16 ~ 21]|6|
|M-BETA|15 ~ 20Hz 미만|left/rightPowerSpectrum[22 ~ 31]|10|
|H-BETA|20 ~ 30Hz 미만|left/rightPowerSpectrum[32 ~ 52]|21|
|GAMMA|30 ~ 40Hz 미만|left/rightPowerSpectrum[53 ~ 73]|21|

<br/>

#### 뇌파 분석 데이터 크기 판정값 활용

뇌파 데이터의 leftThetaIndicatorValue, rightThetaIndicatorValue, leftAlphaIndicatorValue, ... ,
leftRelaxationIndicatorValue, rightRelaxationIndicatorValue, unbalanceIndicatorValue 까지의 프로퍼티는 크기 판정값이 있습니다.

![img.png](img.png)

* unbalanceIndicatorValue를 제외한 나머지 프로퍼티는 5의 값이 표준이며 0에 수렴할수록 매우 낮은 상태를 의미하고,
  10에 수렴할수록 매우 높은 상태를 의미합니다.
* unbalanceIndicatorValue는 5의 값은 좌우뇌 균형을 의미하고 0에 수렴할수록 우뇌가 활성화되었음을 의미하고,
  10에 수렴할수록 좌뇌가 활성화되었음을 의미합니다.

<br/>

#### startMeasuring 함수 매개 변수

> measuringTime : 측정 진행 시간<br/>
> eyeState : 측정하려는 눈의 상태. Opened 와 Closed 두 개의 타입이 존재<br/>
> onError : 에러가 발생했을 때 호출되는 콜백. 인자로 Throwable이 들어옴

#### 사용 예시

```kotlin
// 장치 측정 시작
viewModel.startMeasuring(measuringTime = 60, eyesState = Result.EyesState.CLOSED, onError = { throwable ->
    runOnUiThread {
      Toast.makeText(applicationContext, throwable.message.toString(), Toast.LENGTH_SHORT).show()
    }
})

// 장치 측정 종료
viewModel.stopMeasuring()

// 측정 상태
viewModel.isMeasuring.observe(this@SampleActivity) { value ->
  if (value) {
    // true -> 측정 중
  } else {
    // false -> 대기 중
  }
}

// 뇌파 데이터
viewModel.result.observe(this@SampleActivity) { value ->
    println("Result : $value")
}
```

<br/>

### 장치 정보 획득

장치와의 연결이 성립되면 **장치 시리얼 번호, 측정 상태 변환 시간, 신호 안정화 기준값**을 획득할 수 있습니다.<br/>
다음과 같은 함수를 호출하면 콜백 함수로 결과가 전달됩니다.

* 장치 시리얼 번호 : 장치에 부여된 고유번호(Serial number)
* 측정 상태 변환 시간 : 장치에 적용된 측정 상태 변환 시간 값
* 신호 안정화 기준값 : 장치에 기록된 뇌파 안정화 기준 값

#### 사용 예시
```kotlin
// 장치 시리얼 번호
viewModel.readSerialNo(block = { serialNumber ->
    Toast.makeText(applicationContext, serialNumber, Toast.LENGTH_SHORT).show()
})

// 측정 상태 변환 시간
viewModel.readMeasureStartChangeTime(block = { time ->
    Toast.makeText(applicationContext, time, Toast.LENGTH_SHORT).show()
})

// 신호 안정화 기준값
viewModel.readSignalStability(block = { signalStability ->
    Toast.makeText(applicationContext, signalStability, Toast.LENGTH_SHORT).show()
})
```

<br/>

### 장치 상태 모니터링

다음과 같은 LiveData를 사용하면 현재 장치의 상태를 파악할 수 있습니다.

* electrodeStatus : 전극 연결 상태
* batteryLevel : 배터리 잔량 상태
* eegStabilityValue : 뇌파 안정 상태

#### 전극 센서 부착 상태

```kotlin
/**
 * ALL_DETACHED -> all detached
 * LEFT_ELECTRODE_DETACHED -> left eeg sensor detached
 * RIGHT_ELECTRODE_DETACHED -> right eeg sensor detached
 * LEFT_EARPHONE_DETACHED -> left, right eeg sensor detached
 * RIGHT_EARPHONE_DETACHED -> earphone detached
 * ALL_ATTACHED -> all attached
 */
viewModel.electrodeStatus.observe(this@SampleActivity) { state ->
    when (state) {
        Result.Electrode.ALL_DETACHED              -> {}
        Result.Electrode.LEFT_ELECTRODE_DETACHED   -> {}
        Result.Electrode.RIGHT_ELECTRODE_DETACHED  -> {}
        Result.Electrode.LEFT_EARPHONE_DETACHED    -> {}
        Result.Electrode.RIGHT_EARPHONE_DETACHED   -> {}
        Result.Electrode.ALL_ATTACHED              -> {}
    }
}
```

<br/>

#### 배터리 잔량 상태

```kotlin
/**
 * INSUFFICIENT -> device battery is low
 * SUFFICIENT -> device battery is sufficient
 */
viewModel.batteryLevel.observe(this@SampleActivity) { level ->
    when (level) {
        Result.BatteryLevel.INSUFFICIENT -> {}
        Result.BatteryLevel.SUFFICIENT   -> {}
    }
}
```

<br/>

#### 뇌파 안정 상태

```kotlin
/**
 * UNSTABILIZED -> unstabilized eeg
 * STABILIZED -> stabilized eeg
 */
viewModel.eegStabilityValue.observe(this@MainActivity) { value ->
    when (value) {
        Result.EEGStability.UNSTABILIZED -> {}
        Result.EEGStability.STABILIZED   -> {}
    }
}
```

<br/>

### 라이브러리 참조 설정

#### Gradle 파일(앱 레벨)

```groovy
dependencies {
    implementation 'omnifit.sdk:omnifit-brain-ktx:0.0.2'
}
```

<br/>

#### settings.gradle 파일

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'http://maven.omnicns.co.kr/nexus/content/repositories/releases/'; allowInsecureProtocol true }
    }
}
```

<br/>

## 라이센스

    Copyright 2022 omniC&S

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.