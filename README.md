Material Code input
-----------------

A material style input to put security codes like those two factor authentication codes received by SMS.

![Demo Screenshot](./art/CodeInputView.gif)


Based on
----------

[Code input field concept](http://www.materialup.com/posts/code-input-field-concept) by [SAMUEL KANTALA](http://www.materialup.com/ontidop)

[Code input lib](https://github.com/glomadrian/material-code-input) by [Adrián García Lomas](https://github.com/glomadrian)


How to use
----------

Minimal SDK Version 14

Usage with default colors (default digits are 6) without finish animation

```xml
  <com.raycoarana.codeinputview.CodeInputView
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      app:animate_on_complete="false"
      />
```

Usage with custom colors and attributes using a numeric only input

```xml
<com.raycoarana.codeinputview.CodeInputView
    android:layout_marginTop="20dp"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:underline_color="#457ad1"
    app:underline_selected_color="#9e1ace"
    app:code_text_color="#b12eff"
    app:error_color="#77ce9d"
    app:input_type="numeric"
    app:length_of_code="4"
    />
```

Remember put this for custom attribute usage

```java

xmlns:app="http://schemas.android.com/apk/res-auto"

```

Get the input code as String

```java
  codeInputView.getCode()
```

Set a default code

```java
  codeInputView.setCode("1234")
```

Show an error when the code is not valid

```java
  codeInputView.setError("Ups! Try with other code.")
```

For Gradle
---------------------

Add dependency
```java
compile 'com.raycoarana.codeinputview:codeinputview:1.1.2'
```

License
-------

    Copyright 2016 Rayco Araña

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

