/*
 * runtime.c
 *
 * rotinas utilizadas pelos programas gerados pelo
 * compilador MiniJava.
 */
#include <stdio.h>
#include <stdlib.h>

//#define DEBUG

#ifdef DEBUG

#define print(v) do { printf("MiniJava RUNTIME MESSAGE: "); printf v; printf("\n"); } while(0)

#else

#define print(v)

#endif // ifdef DEBUG

#ifdef __GNUC__

#define minijava_main_1 _minijava_main_1
#define minijavaExit _minijavaExit
#define assertPtr _assertPtr
#define boundCheck _boundCheck
#define newArray _newArray
#define newObject _newObject
#define printInt _printInt

#endif

/*
 * PointerList
 *
 * mantem uma lista com os ponteiros criados
 * pelo runtime
 */
typedef struct PointerListTAG
{
    void* thePointer;            // o ponteiro alocado pelo ambiente
    struct PointerListTAG* next; // lista para o proximo elemento
} PointerList, *PPointerList;

static PPointerList head = NULL;

/*
 * addPointer
 *
 * insere o ponteiro especificado na lista
 * de pointeiros criados pelo ambiente
 *
 * entrada:
 *  ptr: o pointeiro
 *
 * saida:
 *
 */
static void addPointer(void* ptr)
{
    PPointerList aux = (PPointerList) malloc(sizeof(PointerList));

    if ( aux == NULL )
    {
        printf("FATAL: nao foi possivel alocar espaco para o ponteiro na lista de pointeiros");
        exit(-1);
    }

    aux->thePointer = ptr;
    aux->next = head;

    head = aux;
}

/*
 * searchPointer
 *
 * Procura o ponteiro especificado na lista de ponteiros
 * alocados pelo ambiente. Finaliza o programa se nao encontra.
 *
 * entrada:
 *  ptr: o pointeiro
 *  line: linha onde o acesso a ptr esta sendo feito
 *
 * saida:
 *
 */
static void searchPointer(void* ptr, int line)
{
    PPointerList aux = head;

    for (; aux != NULL; aux = aux->next)
        if ( aux->thePointer == ptr )
            return;

    printf("Invalid Pointer @ %d\n", line);
    exit(-1);
}


/*
 * main
 */
extern void minijava_main_1(void);

int main(int argc, char* argv[])
{
   minijava_main_1();

   return 0;
}

/*
 * minijavaExit
 *
 * finaliza a execucao do programa
 *
 * entrada:
 *  status: qual status retornar para o sistema
 *
 * saida:
 *
 */
void minijavaExit
(
    int status
)
{
    print(("minijavaExit(%d)", status));
    exit(status);
}

/*
 * assertPtr
 *
 * verifica se o ponteiro passado eh valido
 *
 * entrada:
 *  ptr = ponteiro a ser testado
 *  line = linha do arquivo fonte em que
 *         esta sendo requisitado o acesso
 *         ao ponteiro
 *
 * saida:
 *
 */
void assertPtr
(
    void* ptr, 
    int line
)
{
    print(("assertPtr(%p,%d)", ptr, line));
    if ( !ptr )
    {
        printf("null pointer exception @ %d\n", line);
        exit(-1);
    }

    searchPointer(ptr, line);
}

/*
 * boundCheck
 *
 * Verifica se o indice solicitado eh valido no vetor
 *
 * entrada:
 *  arr: o vetor
 *  idx: o indice desejado
 *  line: a linha no arquivo fonte que a indexacao esta
 *        ocorrendo.
 *
 * saida:
 *
 */
void boundCheck
(
    int* arr, 
    int idx, 
    int line
)
{
    int size = *arr;

    print(("boundCheck(%p,%d, %d)", arr, idx, line));

    if ( idx >= size || idx < 0 )
    {
        printf("Index out of bounds exception @ %d\n", line);
        exit(-1);
    }
}

/*
 * newObject
 *
 * cria um novo objeto com o tamanho desejado
 *
 * entrada:
 *  size: tamanho total do novo objeto
 *  vtable: endereco da 'vtable' para o objeto.
 *
 * saida:
 *  o endereco do objeto criado
 */
void* newObject
(
    int size, 
    int vtable
)
{
    int* obj = (int*) malloc(sizeof(int) * size);
    print(("newObject(%d,%p)", size, vtable));

    if ( obj != NULL )
    {
        int i;

        *obj = vtable;

        for ( i = 1; i < size; i++ )
            obj[i] = 0;

        addPointer(obj);
    }

    print(("obj = %p", obj));
    return obj;
}

/*
 * newArray
 *
 * cria um novo vetor com o tamanho desejado
 *
 * entrada:
 *  size: tamanho do vetor
 *
 * saida:
 *  o endereco do vetor criado
 */
int* newArray
(
    int size
)
{
    int i;
    int* arr = (int*) malloc(sizeof(int) * (size+1));

    print(("newArray(%d)", size));

    if ( arr != NULL )
    {
        *arr = size;
        for ( i = 1; i <= size; i++ )
            arr[i] = 0;
        addPointer(arr);
    }

    print(("arr = %p", arr));
    return arr;
}

/*
 * printInt
 *
 * imprime o inteiro solicitado na saida padrao
 *
 * entrada:
 *  i: inteiro a ser escrito
 *
 * saida:
 *  
 */
void printInt
(
    int i
)
{
    printf("%d\n", i);
}
