Imports System.IO
Imports System.Text
Imports System.Configuration
'new line adsfasdfasdf dgfgdgf

Public Class clsError
    '-------------------------------------------------------------------------------------------------------------------
    'Project(s)     :       WebBIZ projects (WBError.sln)
    'Workfile       :       clsError.vb
    'Copyright      :       Fortune Informatics Ltd.
    'Created on     :       16 Nov 2006
    'Author         :       P.Raja Ratna Kumar
    'Description    :       Will generate the error log file
    'Dependencies   :       All WebBIZ projects
    'Issues         :
    'Comments       :
    '------------------------------------------------------------------------------------------------------------------

    Public Sub LogErrorMessage(ByVal ErrObj As Exception, ByVal tModDescription As String, Optional ByVal tCustomMessage As String = "", _
    Optional ByVal lLineNum As Integer = 0, Optional ByVal ptFileName As String = "")
        '--------------------------------------------------------------------------------------------------------------
        'Description    :   Will display an error message or
        '                   Will Create the error log file
        '                   Log the error into a Log file. The log file will be created in the folder \Log Files
        '                   of the application path.
        'Pass           :   ErrObj          :   Error object or the calling component
        '                   tModDescription :   The source of error occurred, mostely the event name
        '                   tCustomMessage  :   (Optional) If the developer wants to append his/her comments in the log
        '                   lLineNum        :   (Optional) Line number where exactly error has occurred
        '                   ptFileName      :   (Optional) Name of the file with which name the log file will be created, if not passed
        '                                       (Optional) then the source of error object will be taken as file name
        'Return         :   N/A
        '--------------------------------------------------------------------------------------------------------------
        'Developer      :   P. Raja Ratna Kumar
        'Created on     :   15 Nov 2006
        '--------------------------------------------------------------------------------------------------------------
        Dim tErrorString As String = ""
        Dim tLogFilePath As String = ""
        Dim sbError As StringBuilder = Nothing
        Dim tFolderPath As String = ""

        Try
            'If a message to be displayed
            sbError = New StringBuilder
            If ErrObj Is Nothing Then
                sbError.Append("")
                sbError.Append("`")
                sbError.Append("")
                sbError.Append("`")
                sbError.Append(tModDescription)
                sbError.Append("`")
                sbError.Append(Date.Now.ToString("dd-MM-yyyy HH:mm:ss:ff"))
                sbError.Append("`")
                sbError.Append(lLineNum)
                sbError.Append("`")
                sbError.Append(tCustomMessage)
                sbError.Append("`")
                sbError.Append("")
                tErrorString = sbError.ToString

            ElseIf ErrObj.Message.Trim.Length <> 0 Then
                sbError.Append("Description= ")
                sbError.Append(ErrObj.Message.Trim).AppendLine()
                sbError.Append("`")
                sbError.Append(tCustomMessage)
                sbError.Append("`")
                sbError.Append("Source= = ")
                sbError.Append(ErrObj.Source).AppendLine()
                sbError.Append("Module= ")
                sbError.Append(tModDescription).AppendLine()
                sbError.Append("DateTime= ")
                sbError.Append(Date.Now.ToString("dd-MM-yyyy HH:mm:ss:ff")).AppendLine()
                sbError.Append("=================================================")
                tErrorString = sbError.ToString
                If lLineNum <> 0 Then
                    tErrorString = tErrorString & vbCrLf & "Line #= " & lLineNum
                End If
            ElseIf ErrObj.Message.Trim.Length <> 0 Or tCustomMessage.Trim.Length > 0 Then
                sbError.Append(ErrObj.Message)
                sbError.Append("`")
                sbError.Append(ErrObj.Source)
                sbError.Append("`")
                sbError.Append(tModDescription)
                sbError.Append("`")
                sbError.Append(Date.Now.ToString("dd-MM-yyyy HH:mm:ss:ff"))
                sbError.Append("`")
                sbError.Append(lLineNum)
                sbError.Append("`")
                sbError.Append(tCustomMessage)
                sbError.Append("`")
                sbError.Append("")
                tErrorString = sbError.ToString
            Else
                Exit Try
            End If
            sbError = Nothing
            '<<Update/Create the error log file
            If ptFileName.Trim.Length = 0 Then
                ptFileName = "WBZ5_Error.log"
            Else
                ptFileName = String.Concat(ptFileName, ".log")
            End If

            If ErrObj IsNot Nothing Then ErrObj.Data.Clear()

            tFolderPath = getFolderPath()
            tLogFilePath = CreateLogFolder(ptFileName, tFolderPath)  'Get the log file path
            Call WriteToFile(tErrorString, tLogFilePath)
            '>>

        Catch ex As Exception
        Finally
            If sbError IsNot Nothing Then sbError = Nothing
        End Try

    End Sub

    Private Function CreateLogFolder(ByVal tFileName As String, ByVal tFolderPath As String) As String
        '--------------------------------------------------------------------------------------------------------------
        'Description    :   Will create folder to store error log files if not existed.
        '                   The folder will create in Bin folder of application.
        'Pass           :   tFileName       :   Name of the error log file
        'Return         :   CreateLogFolder :   The file where the error message to be written
        '--------------------------------------------------------------------------------------------------------------
        'Developer      :   P. Raja Ratna Kumar, Ali
        'Created on     :   16 Nov 2006
        '--------------------------------------------------------------------------------------------------------------
        Dim drLogFolder As New DirectoryInfo(tFolderPath)
        Dim tFilePathInfo As FileInfo = Nothing
        Dim tFilePath As String

        CreateLogFolder = ""
        Try

            If Not drLogFolder.Exists Then
                drLogFolder.Create()
            End If

            tFilePath = String.Concat(drLogFolder.FullName, "\", tFileName)
            tFilePathInfo = New FileInfo(tFilePath)

            CreateLogFolder = tFilePathInfo.FullName
            'Check if the file size is greater than 1 MB,
            'if greater, then copy the file with other name(ie. including the date and time)
            'And then delete the old file

            If tFilePathInfo.Exists Then
                If tFilePathInfo.Length > 102400 Then
                    tFilePathInfo.CopyTo(Mid(tFilePath, 1, Len(tFilePath) - 4) & "_" & Format(Now, "dd-MMM-yyyy hh-mm-ss") & ".log")
                    Kill(tFilePathInfo.FullName)
                    tFilePathInfo = Nothing
                End If
            End If

        Catch ex As Exception

        Finally
            If drLogFolder IsNot Nothing Then drLogFolder = Nothing
            If tFilePathInfo IsNot Nothing Then tFilePathInfo = Nothing
        End Try
    End Function

    Private Sub WriteToFile(ByVal tMessage As String, ByVal tFilePath As String)
        '---------------------------------------------------------------------------------------------------------
        'Description    :   Will write the passed message to a file
        '                   - If the file is not existing then create the file and then write
        'Pass           :   tMessage    :   Will have the messge to be written to a file
        '                   tFilePath   :   The file in which the message to be written
        'Return         :   N/A
        '---------------------------------------------------------------------------------------------------------
        'Developer      :   Ali
        'Created on     :   16 Nov 1006
        '---------------------------------------------------------------------------------------------------------

        Dim fsExport As FileStream = Nothing
        Dim swExport As StreamWriter = Nothing 'Streamwriter

        Try
            'Will append or create a new file with the message passed
            fsExport = New FileStream(tFilePath, FileMode.Append, FileAccess.Write)
            swExport = New StreamWriter(fsExport)
            swExport.Write(tMessage & vbCrLf)

            swExport.Close()
            swExport.Dispose()
            fsExport.Close()
            fsExport.Dispose()

        Catch ex As Exception
        Finally
            If swExport IsNot Nothing Then swExport = Nothing
            If fsExport IsNot Nothing Then fsExport = Nothing
        End Try

    End Sub

    Public Sub LogMessage(ByVal tModDescription As String, ByVal tMessage As String, ByVal ptFileName As String)
        Dim tErrorString As String = ""
        Dim tLogFilePath As String = ""
        Dim sbBuilder As StringBuilder = Nothing
        Dim tFolderPath As String = ""
        Try
            sbBuilder = New StringBuilder
            sbBuilder.Append("Description= ")
            sbBuilder.Append(tMessage).AppendLine()
            sbBuilder.Append("Module= ")
            sbBuilder.Append(tModDescription).AppendLine()
            sbBuilder.Append("DateTime= ")
            sbBuilder.Append(Date.Now.ToString("dd-MM-yyyy HH:mm:ss:ff")).AppendLine()
            sbBuilder.Append("=================================================")
            tErrorString = sbBuilder.ToString
            sbBuilder = Nothing
            '<<Update/Create the error log file
            If ptFileName.Trim.Length = 0 Then
                ptFileName = "WBZ5_Error.log"
            Else
                ptFileName = String.Concat(ptFileName, ".log")
            End If
            tFolderPath = getFolderPath()
            tLogFilePath = CreateLogFolder(ptFileName, tFolderPath)  'Get the log file path
            Call WriteToFile(tErrorString, tLogFilePath)
        Catch ex As Exception
        Finally
            If sbBuilder IsNot Nothing Then sbBuilder = Nothing
        End Try
    End Sub

    Private Function getFolderPath() As String
        Dim tPath As String = AppDomain.CurrentDomain.SetupInformation.ApplicationBase
        'Dim tXMLPath As String = Path.Combine(tPath, "AppLogPath.xml")
        getFolderPath = tPath & "Log Files"

        Try
            If ConfigurationManager.AppSettings("LogFolderPath") IsNot Nothing Then
                If ConfigurationManager.AppSettings("LogFolderPath").Trim.Length > 0 Then
                    getFolderPath = ConfigurationManager.AppSettings("LogFolderPath").Trim
                Else
                    getFolderPath = AppDomain.CurrentDomain.SetupInformation.ApplicationBase & "Log Files"
                End If
            Else
                getFolderPath = AppDomain.CurrentDomain.SetupInformation.ApplicationBase & "Log Files"
            End If
            'Using reader As XmlReader = XmlReader.Create(tXMLPath)
            '    While reader.Read
            '        If reader.NodeType = XmlNodeType.Text Then 'read first element value and exit
            '            getFolderPath = reader.Value
            '            Exit While
            '        End If
            '    End While
            'End Using
        Catch ex As Exception
            getFolderPath = AppDomain.CurrentDomain.SetupInformation.ApplicationBase & "Log Files"
        End Try
    End Function

End Class
