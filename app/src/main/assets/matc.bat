REM matc -p mobile -a opengl -o ./materials/lit.filamat ./materials/lit.mat
REM matc -p mobile -a opengl -o ./materials/emissive_colored.filamat ./materials/emissive_colored.mat
for /R %%G in (.\materials\*.mat) do matc --optimize-size --platform=mobile -o ".\materials\%%~nG.filamat" "%%G"
pause