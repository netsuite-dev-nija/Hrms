@echo off

for %%i in (
customscript_hris_emsetholiday_ue
customscript_hris_emp_seg_ue
customscript_hris_empcodegen_ue
customscript_hris_empcreateleavebal_ue
customscript_hris_empcreatedata_ue
customscript_hris_empinactivaterecord_ue
customscript_hris_empdisableedit_ue
customscript_hris_emptoempcompchange_ue
customscript_hris_post_emp_details_ues
) do (
    suitecloud object:import --type ALL --destinationfolder "/Objects" --scriptid %%i
)