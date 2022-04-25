Nesta fase do projeto, com a possibilidade de fornecer um serviço através de vários servidores, deparámo-nos com
problemas de coerência que originavam conflitos. Este conflitos davam-se pela razão de a operação de incrição poder ser
feita em qualquer um dos servidores. Estes servidores, propagavam os seus estados para os outros existentes, de dois em
dois segundos. Para combater estes problemas de coerência, utilizou-se um método de coerência temporal. Implementámos um
método de resolução de conflitos que chamámos de "merge". Este método, recebe um estado e um boolean que nos diz se esse
estado vem de um servidor primário. Do estado atual e do novo estado recebido, precisamos de todos os alunos que estão
nas listas de enrolled e discarded, para podermos tratar dos conflitos existentes. Utilizámos timestamps para termos
noção de quando os alunos foram inscritos/discartados, para através disso, podermos ter um critério de desempate no
momento em que hajam conflitos, assim garantimos que temos o estado mais atualizado. Por exemplo, no caso descrito no
enunciado, de acordo com a nossa implementação, ganhariam os inscritos que tivessem os timestamps mais baixos i.e o
aluno que se inscreveu primeiro, tem prioridade sobre o aluno que se inscreveu mais tarde. Por outro lado, no caso de um
aluno num estado estar inscrito e noutro estado estar discartado, aí ganha o estado mais recente i.e a alteração mais
recente é a mais atualizada. Utilizámos também timestamps para sabermos quando foi fechado o processo de inscrições,
para garantir que nenhum aluno se conseguisse inscrever depois desse mesmo fecho. Depois de termos todos os conflitos
resolvidos, propaga-se então um estado novo coerente. Quando um gossip é desativado e ocorrem alterações, mais tarde
quando for ativado novamente, através do algoritmo descrito em cima, o estado do servidor é atualizado na próxima
propagação.
