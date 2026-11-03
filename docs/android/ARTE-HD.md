# Arte detalhada dos pets — desenvolvimento 0.17

O atlas `android/app/src/main/assets/art/pets-hd.png` foi gerado a partir das referências fornecidas pelo mantenedor, com fundo alfa real e rostos sem olhos. Não é uma cópia pixel a pixel das referências. O desenho inclui Bryan, Jorge, Mimir, Luna e Presidente, nessa ordem em uma grade 3×2; a última célula fica vazia.

`PetArt` mantém cache limitado a oito combinações e recolore os pixels saturados da moldura, preservando sombras, brilho e acessórios. Não se faz processamento de imagem a cada frame. Os olhos são desenhados por `PetView`, com expressões, piscar e cor independente. O atlas é compartilhado entre prévia, overlay e plataforma do Arkanoid.

O visual original usa a arte detalhada. Selecionar outro acessório usa a base detalhada de Bryan e o acessório desenhado pelo app; isso permite trocar ou remover itens sem deixar o item original preso na imagem. Separar todos os acessórios em camadas de igual qualidade é um próximo passo. Preferências de cores e memórias são preservadas.

A arte raster é adaptada, não um modelo 3D. As imagens de referência e a geração não garantem identidade pixel a pixel. Validar no aparelho arraste, inclinação, tamanho, desempenho e transparência sobre apps claros e escuros.
