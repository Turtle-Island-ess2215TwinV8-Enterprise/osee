Option Explicit

Dim oWord
Dim baseDoc
Dim compareDoc
Dim authorName
Dim detectFormatChanges
Dim ver1
Dim ver2
Dim wdCompareTargetSelectedDiff
Dim wdCompareTargetSelectedMerge
Dim wdFormattingFromCurrent
Dim wdFormatXML
Dim wdDoNotSaveChanges
Dim mainDoc

Public Sub main()
 wdCompareTargetSelectedDiff = 0
    wdCompareTargetSelectedMerge = 1
    wdDoNotSaveChanges = 0
    wdFormattingFromCurrent = 3
    wdFormatXML = 11

    authorName = "OSEE Doc compare"
    set oWord = WScript.CreateObject("Word.Application")
    oWord.Visible = False
    detectFormatChanges = false

WScript.sleep(250)
    ver1 = "##SRC_FILE1##"
    ver2 = "##SRC_FILE2##"

    set baseDoc = oWord.Documents.Open (ver1)
    baseDoc.TrackRevisions = false
    baseDoc.AcceptAllRevisions


    set compareDoc = oWord.Documents.Open (ver2)
    compareDoc.AcceptAllRevisions
    compareDoc.TrackRevisions = false
    compareDoc.Save
    compareDoc.Close


    baseDoc.Compare ver2, authorName, wdCompareTargetSelectedDiff, detectFormatChanges, False, False
    set compareDoc = oWord.ActiveDocument

    set mainDoc = compareDoc
    baseDoc.close
    set baseDoc = Nothing
    oWord.NormalTemplate.Saved = True
    mainDoc.SaveAs "##DIFF_OUTPUT##", wdFormatXML, , , False

        oWord.Quit()
        set oWord = Nothing
End Sub

main